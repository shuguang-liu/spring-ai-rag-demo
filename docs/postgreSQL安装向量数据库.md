# 安装向量数据库-postgresql

Rocky Linux 10 不能用Docker，那我们直接通过官方YUM仓库安装 PostgreSQL 16 + pgvector 扩展。整个过程分两大步：**装PostgreSQL** 和 **装pgvector扩展**。

------

## **第1步：安装 PostgreSQL 官方YUM仓库**

```bash
# Rocky Linux 10 属于 EL-10 系列，用对应的仓库RPM
sudo dnf install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-10-x86_64/pgdg-redhat-repo-latest.noarch.rpm
```

> 如果上面的链接下载失败（网络问题），可以试试阿里云镜像：
>
> ```bash
> sudo rpm -Uvh https://mirrors.aliyun.com/postgresql/repos/yum/reporpms/EL-$(rpm -E %{rhel})-x86_64/pgdg-redhat-repo-latest.noarch.rpm
> ```

------

## **第2步：禁用系统自带的PostgreSQL模块**

```bash
# Rocky Linux 10 自带了一个旧版PostgreSQL模块，必须禁用，否则会冲突
sudo dnf -qy module disable postgresql
```

------

## **第3步：安装 PostgreSQL 16**

```bash
# 安装服务端和contrib工具包
sudo dnf install -y postgresql16-server postgresql16-contrib
```

------

## **第4步：初始化数据库**

```bash
# 初始化数据目录（默认在 /var/lib/pgsql/16/data）
sudo /usr/pgsql-16/bin/postgresql-16-setup initdb
```

预期输出：

```
Initializing database ...  success
```

------

## **第5步：启动并设置开机自启**

```bash
sudo systemctl enable postgresql-16
sudo systemctl start postgresql-16

# 验证运行状态
sudo systemctl status postgresql-16
```

预期看到 `active (running)` 就表示启动成功。

------

## **第6步：配置远程访问（可选）**

如果你需要从其他机器连接，或者Spring Boot应用和数据库不在同一台机器上，需要修改配置：

### **6.1 修改监听地址**

```bash
sudo vi /var/lib/pgsql/16/data/postgresql.conf
```

找到以下两行，去掉注释并修改：

```
listen_addresses = '*'
port = 5432
```

### **6.2 修改认证规则**

```bash
sudo vi /var/lib/pgsql/16/data/pg_hba.conf
```

在文件末尾添加一行（允许所有IP用密码连接）：

```
host    all             all             0.0.0.0/0               md5
```

### **6.3 开放防火墙端口**

```bash
# 先检查防火墙是否开启
systemctl status firewalld

# 如果显示 active (running)，则开放5432端口
sudo firewall-cmd --permanent --add-port=5432/tcp
sudo firewall-cmd --reload

# 验证
sudo firewall-cmd --list-ports
```

### **6.4 重启使配置生效**

```bash
sudo systemctl restart postgresql-16
```

## **第7步：设置postgres用户密码**

```bash
# 切换到postgres系统用户，登录数据库
sudo -u postgres psql
# 这一步如果你修改了端口号，会报错。因为在执行这个命令之后，会查询默认端口号。
# 可以使用这个命令
sudo -u postgres psql -p port<修改后的端口号>
```

```sql
-- 在psql中执行：
ALTER USER postgres WITH PASSWORD 'your_password';

-- 创建ai_demo数据库（Spring AI项目要用）
CREATE DATABASE ai_demo;

-- 退出
\q
```

------

## **第8步：验证PostgreSQL安装成功**

```bash
# 查看版本
psql -V
# 预期输出：psql (PostgreSQL) 16.x

# 登录测试
sudo -u postgres psql -d ai_demo
```

```sql
-- 查看版本
SELECT version();
-- 预期：PostgreSQL 16.x on x86_64-...

\q
```

到这里，PostgreSQL 16 就安装好了。

## **第9步：安装pgvector扩展**

pgvector有两种安装方式：**YUM包安装**（简单）和**源码编译**（灵活）。推荐先试YUM方式，不行再用源码。

### **方式一：YUM包安装（推荐，最简单）**

```bash
# 直接安装pgvector扩展包（PGDG仓库已包含）
sudo dnf install -y pgvector_16
```

验证安装：

```bash
ls -l /usr/pgsql-16/lib/vector.so
# 预期看到文件存在
```

### **方式二：源码编译安装（YUM安装失败时用这个）**

```bash
# 第1步：安装编译依赖
sudo dnf install -y gcc make postgresql16-devel git

# 第2步：下载pgvector源码
cd /tmp
git clone --branch v0.8.0 https://github.com/pgvector/pgvector.git
cd pgvector

# 第3步：编译安装
make
sudo make install
```

> 如果 `git clone` 连不上 GitHub，可以用镜像：
>
> ```bash
> git clone --branch v0.8.0 https://gitcode.com/GitHub_Trending/pg/pgvector.git
> ```

验证安装：

```bash
ls -l /usr/pgsql-16/share/extension/vector*
# 预期看到：vector.control、vector--0.8.0.sql 等文件
```

------

## **第10步：在数据库中启用pgvector扩展**

```bash
sudo -u postgres psql -d ai_demo
```

```sql
-- 启用pgvector扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 验证扩展是否加载成功
SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';
-- 预期输出：vector | 0.8.0（或更高版本）

-- 测试向量功能是否正常
SELECT '[1,2,3]'::vector;
-- 预期输出：[1,2,3]

-- 测试向量距离计算
SELECT '[1,2,3]'::vector <=> '[3,2,1]'::vector AS cosine_distance;
-- 预期输出：一个0~2之间的浮点数

-- 查看pgvector提供的所有函数
\df *vector*
-- 预期看到：cosine_distance、l2_distance、inner_product 等

\q
```

## **第11步：快速功能验证**

```bash
sudo -u postgres psql -d ai_demo
```

```sql
-- 创建测试表（3维向量，方便验证）
CREATE TABLE test_items (
    id SERIAL PRIMARY KEY,
    content TEXT,
    embedding VECTOR(3)
);

-- 插入测试数据
INSERT INTO test_items (content, embedding) VALUES
    ('PostgreSQL数据库', '[0.1, 0.2, 0.3]'),
    ('向量搜索引擎', '[0.2, 0.3, 0.4]'),
    ('机器学习AI', '[0.8, 0.9, 0.7]'),
    ('数据科学工具', '[0.7, 0.8, 0.6]');

-- 语义检索：找和 [0.15, 0.25, 0.35] 最相似的文档
SELECT id, content, embedding <=> '[0.15, 0.25, 0.35]'::vector AS distance
FROM test_items
ORDER BY distance
LIMIT 3;

-- 预期输出：
--  id |     content      |    distance
-- ----+------------------+-----------------
--   2 | 向量搜索引擎      | 0.0346...
--   1 | PostgreSQL数据库   | 0.0519...
--   4 | 数据科学工具       | 0.9234...
-- 距离越小 = 越相似

\q
```

## **完整安装流程回顾**

```
第1步：安装PGDG官方YUM仓库
第2步：禁用系统自带的PostgreSQL模块
第3步：安装 postgresql16-server + contrib
第4步：初始化数据库（initdb）
第5步：启动服务 + 开机自启
第6步：配置远程访问（可选）
第7步：设置postgres密码 + 创建ai_demo数据库
第8步：验证PostgreSQL安装
────────────────────────────────────
第9步：安装pgvector扩展（YUM或源码编译）
第10步：在ai_demo数据库中启用vector扩展
第11步：创建测试表，验证向量检索功能
```


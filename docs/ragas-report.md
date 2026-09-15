# D5 RAGAS 评估报告

> 测试集 20 条 | 评估模型 qwen-plus | 指标：faithfulness / answer_relevancy / context_precision / context_recall

## 一、四指标平均值

| 指标 | 平均值 | 说明 |
|---|---|---|
| faithfulness | 0.82 | 回答是否忠于检索上下文 |
| answer_relevancy | 0.75 | 回答是否切题 |
| context_precision | 0.72 | 检索的上下文是否精准 |
| context_recall | 0.79 | 该召回的有没有召回 |

## 二、逐条明细

| # | 问题 | faithfulness | answer_relevancy | context_precision | context_recall |
|---|---|---|---|---|---|
| 1 | LangChain 的核心定位是什么？ | 0.95 | 0.93 | 1.00 | 0.00 |
| 2 | LangGraph 的核心总结？ | 0.96 | 0.96 | 0.00 | 0.00 |
| 3 | AutoGen 是什么？ | 1.00 | 0.91 | 1.00 | 1.00 |
| 4 | LlamaIndex 的核心定位是什么？ | 1.00 | 0.94 | 1.00 | 1.00 |
| 5 | MCP 是什么？ | — | 0.69 | 0.95 | 1.00 |
| 6 | SSE 的核心本质是什么？ | 1.00 | 0.91 | 0.95 | 1.00 |
| 7 | LangGraph 和 LangChain 的区别是什么？ | 0.97 | 0.97 | 1.00 | 0.25 |
| 8 | RAG 和 rerank 的区别和联系？ | — | 0.95 | 1.00 | 0.67 |
| 9 | SSE 和 MCP 的区别和联系？ | 0.96 | 0.93 | 0.00 | 0.50 |
| 10 | LangChain Memory 和 LangGraph 的 Checkpoin... | — | 0.96 | 0.00 | 0.75 |
| 11 | LlamaIndex 和 LangChain 的区别和联系？ | 0.91 | 0.97 | 0.95 | 0.67 |
| 12 | LLM 是 MCP Server？ | 1.00 | 0.97 | 1.00 | 1.00 |
| 13 | MCP 与 Function Calling 是竞争关系？ | 0.94 | 0.85 | 1.00 | 1.00 |
| 14 | 什么是纳什均衡？ | — | 0.88 | 1.00 | 1.00 |
| 15 | 什么是夏普利值？ | 1.00 | 0.70 | 1.00 | 1.00 |
| 16 | 今天北京天气如何？ | 0.00 | 0.00 | 0.00 | 1.00 |
| 17 | 今天吃了几个苹果？ | 0.50 | 0.00 | 0.50 | 1.00 |
| 18 | 今天在幼儿园好好吃饭了吗？ | 0.00 | 0.00 | 0.00 | 1.00 |
| 19 | 啥是博弈论啊？ | 1.00 | 0.72 | 1.00 | 1.00 |
| 20 | 大模型幻觉能根除吗？ | 1.00 | 0.78 | 1.00 | 1.00 |

## 三、Bad Case 详情（faithfulness < 0.5）

### 第 5 条：MCP 是什么？

**faithfulness**: —

**系统回答**：

> MCP 是 Model Context Protocol，即模型上下文协议。  它是一个标准化协议，用于让 AI 像插 USB 线一样即插即用地连接外部工具和数据源。简单来说，MCP 解决的是“AI 与外部工具/数据之间的连接”问题。  它的核心价值是把 M×N 的对接成本降为 M+N。  没有 MCP 时： 如果有 M 个 AI 应用，要对接 N 个工具，就需要写 M×N 套对接代码。  有 MCP 后： 工具提供方只需要实现一个 MCP Server，AI 应用只需要实现一个 MCP Client，任何 MCP Client 都可以调用任何 MCP Server，因此只需要 M+N 套代码。  可以用一句话理解： MCP 是 AI 领域的 USB 协议，也是 AI 与外部工具/数据之间的标准化“万能插头”。  从 Java 后端的角度类比，MCP 类似于 Dubbo 或 gRPC，但区...

**参考答案**：

> MCP（Model Context Protocol，模型上下文协议）是一个标准化协议，让 AI 像插 USB 线一样即插即用地获取外部数据（Resources）和调用工具（Tools）

---

### 第 8 条：RAG 和 rerank 的区别和联系？

**faithfulness**: —

**系统回答**：

> RAG 和 Rerank 不是同一层级的概念：RAG 是一套完整的“检索增强生成”系统架构，而 Rerank 是 RAG 链路中的一个优化环节。  区别：  1. 概念范围不同   RAG 是指把大模型与外部知识库结合的整体方案，核心流程通常包括：用户提问、查询改写、检索召回、重排序、上下文拼装、大模型生成回答。   Rerank 是指重排序，是对已经召回来的候选文档进行二次精细排序，用来把更相关的文档排到前面。  2. 所处位置不同   RAG 覆盖从数据处理、索引、检索、生成到评估的完整链路。   Rerank 通常位于“检索之后、生成之前”。先用向量检索、BM25 或混合检索召回一批候选文档，再用 Rerank 模型对这些候选文档重新打分。  3. 解决问题不同   RAG 解决的是大模型知识滞后、缺少私有知识、容易幻觉的问题，通过先检索外部知识再生成答案。   Rerank 解决的...

**参考答案**：

> RAG（Retrieval-Augmented Generation，检索增强生成）的本质是将大模型（LLM）与外部知识库结合，解决大模型知识滞后和幻觉问题。重排序是解决大模型信息包含的噪声、注意力分散和上下文产生幻觉。rerank 是解决 RAG 流程的中一个问题

---

### 第 10 条：LangChain Memory 和 LangGraph 的 Checkpointer 有什么区别？

**faithfulness**: —

**系统回答**：

> LangChain Memory 和 LangGraph 的 Checkpointer 的核心区别是：Memory 主要负责“对话历史/上下文记忆”，Checkpointer 主要负责“图执行状态的存档、恢复和版本管理”。  具体区别如下：  1. 定位不同  LangChain Memory 的本质是“上下文拼接器 + 缓存管理器”。它解决的是 LLM 本身无状态的问题，把历史对话按 session_id 拉出来，拼进 Prompt，再发给大模型。  LangGraph Checkpointer 的本质是“游戏存档系统”，工程上等同于“带版本控制的分布式 Session 管理器”。它服务于 LangGraph 的状态图执行流程，负责保存每个节点执行后的状态快照，并支持恢复、回滚、人工介入等能力。  2. 管理对象不同  LangChain Memory 主要管理多轮对话消息历史，例如用户...

**参考答案**：

> LangChain 的 Memory 本质是上下文拼接器 + 缓存管理器，分三层：第 1 层：核心存储层 ChatMessageHistory（≈ Session/Redis）- 默认存 Python 进程内存（相当于 HashMap），重启即失；- 生产环境用 RedisChatMessageHistory 或 PostgreSQL 持久化，支持多用户、多会话（Session ID）隔离。第 2 层：核心调度层 RunnableWithMessageHistory（≈ Spring Filter/Interceptor）- 作为包装器拦截请求：按 session_id 自动拉历史消息 → 拼进 Prompt → 发大模型 → 返回后自动把新消息存回存储层；- 业务逻辑和状态管理完全解耦，符合单一职责原则。第 3 层：策略层 Memory Classes（≈ 缓存淘汰/压缩算法）Check...

---

### 第 14 条：什么是纳什均衡？

**faithfulness**: —

**系统回答**：

> 纳什均衡（Nash Equilibrium）是一种策略稳定状态：当所有智能体都选定最优策略后，没有任何一个智能体能够通过“单方面改变策略”获得更多好处。  它不是“按劳分配”，而是“我不动，因为改变对我没好处”。例如，囚徒困境中的“双输僵局”也是一种纳什均衡。  信息来源：参考资料中的“纳什均衡（Nash Equilibrium）”相关内容。

**参考答案**：

> 一种策略稳定状态——所有智能体都选定最优策略后，没有任何一个能通过单方面改变策略获得更多好处。这不是按劳分配，而是我不动，因为改变对我没好处（囚徒困境的双输僵局也是纳什均衡

---

### 第 16 条：今天北京天气如何？

**faithfulness**: 0.00

**系统回答**：

> 知识库中未找到相关信息。  信息来源：提供的参考资料

**参考答案**：

> 知识库中未找到相关信息

---

### 第 18 条：今天在幼儿园好好吃饭了吗？

**faithfulness**: 0.00

**系统回答**：

> 知识库中未找到相关信息。  信息来源：提供的参考资料

**参考答案**：

> 知识库中未找到相关信息

---

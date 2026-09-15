![image-20260915011812682](https://files.seeusercontent.com/2026/09/14/Glp6/image-20260915011812682.png)

![image-20260915011817323](https://files.seeusercontent.com/2026/09/14/yB6f/image-20260915011817323.png)

![image-20260915011907762](https://files.seeusercontent.com/2026/09/14/Pg7w/image-20260915011907762.png)





# SSE 三字段协议

## 字段定义

| 字段    | 类型   | 含义                                                 |
| ------- | ------ | ---------------------------------------------------- |
| state   | 枚举   | 当前阶段：RETRIEVING/RERANKING/GENERATING/DONE/ERROR |
| think   | string | 中间过程，如"召回20条，Rerank后保留5条"              |
| message | string | LLM 生成的内容增量                                   |

## 事件序列（典型）

{"state":"RETRIEVING"}
{"state":"RERANKING"}
{"think":"召回20条，Rerank后保留5条"}
{"state":"GENERATING"}
{"message":"Lang"}
{"message":"Chain"}
...
{"state":"DONE"}

## 接口

- SseEmitter: GET /api/ask/stream?question=xxx
- Flux:       GET /api/ask/stream/flux?question=xxx


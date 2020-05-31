package com.github.edgar615.id.snowflake.dao;

import com.github.edgar615.id.snowflake.WorkerNode;

public interface WorkerNodeDao {

    /**
     * 将节点加入到WorkerNode，如果一个节点
     * @param workerNode
     * @return
     */
    int addWorkerNode(WorkerNode workerNode);

    /**
     * 根据IP查询对应的worker node
     * @param host 主机IP
     * @return workerNode
     */
    WorkerNode queryWorkerNode(String host);
}

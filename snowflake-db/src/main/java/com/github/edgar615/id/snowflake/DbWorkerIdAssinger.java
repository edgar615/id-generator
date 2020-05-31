package com.github.edgar615.id.snowflake;

import com.github.edgar615.id.Utils;
import com.github.edgar615.id.WorkerIdAssigner;
import com.github.edgar615.id.snowflake.dao.WorkerNodeDao;

import java.time.Instant;

/**
 * 使用数据库计算workerid，为简化设置，预先在数据库中插入数据，后续只需要针对数据做update即可
 *
 * 因为不能保证两个节点使用同一个host，所以在发现host已存在时直接报错，修复数据后才能启动.
 */
public class DbWorkerIdAssinger implements WorkerIdAssigner {

    private static final Integer TYPE_ACTUAL = 1;

    private static final Integer STATE_AVAILABLE = 2;

    private final WorkerNodeDao workerNodeDao;

    public DbWorkerIdAssinger(WorkerNodeDao workerNodeDao) {
        this.workerNodeDao = workerNodeDao;
    }

    @Override
    public int assignWorkerId() {
        String host = Utils.getIpv4();
        WorkerNode workerNode = workerNodeDao.queryWorkerNode(host);
        if (workerNode != null && workerNode.getState().equals(STATE_AVAILABLE)) {
            // 因为不能保证两个节点使用同一个host，所以在发现host已存在时直接报错，修复数据后才能启动.
            throw new IllegalStateException(String.format("host %s already exists", host));
        }
        // 暂时不考虑docker
        workerNode.setType(TYPE_ACTUAL);
        workerNode.setState(STATE_AVAILABLE);
        workerNode.setLaunchTime(Instant.now().getEpochSecond());
        workerNode.setModifidTime(Instant.now().getEpochSecond());
        return workerNodeDao.addWorkerNode(workerNode);
    }
}

package com.github.edgar615.id.snowflake;

import java.io.Serializable;
import java.util.Date;

public class WorkerNode implements Serializable {

    private Integer workerNodeId;

    private String nodeHost;

    private Integer type;

    private Integer state;

    private Long launchTime;

    private Long modifidTime;

    private Date createdOn;

    public Integer getWorkerNodeId() {
        return workerNodeId;
    }

    public void setWorkerNodeId(Integer workerNodeId) {
        this.workerNodeId = workerNodeId;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getLaunchTime() {
        return launchTime;
    }

    public void setLaunchTime(Long launchTime) {
        this.launchTime = launchTime;
    }

    public Long getModifidTime() {
        return modifidTime;
    }

    public void setModifidTime(Long modifidTime) {
        this.modifidTime = modifidTime;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
}

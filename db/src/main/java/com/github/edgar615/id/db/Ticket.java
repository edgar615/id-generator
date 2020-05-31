package com.github.edgar615.id.db;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long ticketId;

    private String bizType;
    
    private Long minTicket;
    
    private Long maxTicket;
    
    private Integer step;
    
    private String remark;
    
    private Long addTime;
    
    private Integer version;
    
    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }
    
    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }
    
    public Long getMinTicket() {
        return minTicket;
    }

    public void setMinTicket(Long minTicket) {
        this.minTicket = minTicket;
    }
    
    public Long getMaxTicket() {
        return maxTicket;
    }

    public void setMaxTicket(Long maxTicket) {
        this.maxTicket = maxTicket;
    }
    
    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }
    
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public Long getAddTime() {
        return addTime;
    }

    public void setAddTime(Long addTime) {
        this.addTime = addTime;
    }
    
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Ticket")
            .add("ticketId",  ticketId)
            .add("bizType",  bizType)
            .add("minTicket",  minTicket)
            .add("maxTicket",  maxTicket)
            .add("step",  step)
            .add("remark",  remark)
            .add("addTime",  addTime)
            .add("version",  version)
           .toString();
    }
}
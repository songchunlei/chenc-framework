package com.chens.file.vo;

import java.io.Serializable;

/**
 * 文件数据-用于服务与服务之间的传输
 *
 * @author songchunlei@qq.com
 * @create 2018/4/17
 */
public class FileData implements Serializable{

    /**
     * 文件组id
     */
    private String groupId;

    /**
     * 文件原始名称
     */
    private String orgName;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    /**
     * 大小
     */
    private Long size;

    /**
     * md5-用于校验文件完整性
     */
    private String md5;

    /**
     * 文件数据
     */
    private byte[] data;

    public FileData() {

    }

    public FileData(String groupId,String name, String orgName, String type, Long size, String md5,byte[] data) {
        this.groupId = groupId;
        this.name = name;
        this.orgName = orgName;
        this.type = type;
        this.size = size;
        this.data = data;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}

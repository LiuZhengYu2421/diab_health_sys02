package com.imut.diab_health_sys02.entity;

import lombok.Data;

/**
 * 医生信息，对应表 doctor_information
 */
@Data
public class DoctorInformation {

    /** 医生 id */
    private Integer infoId;

    /** 医生姓名 */
    private String doctorName;

    /** 科室 */
    private String department;

    /** 职称 */
    private String title;

    /** 医生简介 */
    private String introduction;

    /** 医生头像 */
    private String imageUrl;

    /** 聊天 token（对接 AI 医生） */
    private String chatToken;
}

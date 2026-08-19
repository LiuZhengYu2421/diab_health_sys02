package com.imut.diab_health_sys01.entity;

import lombok.Data;

/**
 * 糖尿病类型，对应表 diabetes_types
 */
@Data
public class DiabetesType {

    /** 类型 id */
    private Integer typeId;

    /** 类型名称（如 1型、2型、妊娠期...） */
    private String typeName;

    /** 图片 */
    private String img;

    /** 发病机制 */
    private String pathogenesis;

    /** 临床表现 */
    private String manifestation;

    /** 治疗方法 */
    private String treatment;
}

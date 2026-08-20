-- ============================================================
-- 牛牛记账（niuniu-account）建库建表脚本
-- 执行方式：mysql -uroot -p < init.sql
-- 说明：niuniu_account 为业务库；niuniu_account_test 为单元测试专用库
-- ============================================================

CREATE DATABASE IF NOT EXISTS niuniu_account DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS niuniu_account_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- ============================================================
-- 用户表
-- ============================================================
USE niuniu_account;

DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    user_id     VARCHAR(64)  NOT NULL COMMENT '用户ID（u_开头）',
    openid      VARCHAR(64)  NOT NULL COMMENT '微信openid',
    nick_name   VARCHAR(64)  NOT NULL COMMENT '用户昵称',
    avatar_url  VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    login_time  BIGINT       DEFAULT NULL COMMENT '最近登录时间戳（毫秒）',
    create_by   VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（1=正常 0=删除）',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_openid (openid),
    KEY idx_status (status)
) ENGINE = InnoDB COMMENT = '用户表';

-- ============================================================
-- 账单表
-- ============================================================
DROP TABLE IF EXISTS t_bill;
CREATE TABLE t_bill (
    id            VARCHAR(64)  NOT NULL COMMENT '账单ID（b_开头）',
    user_id       VARCHAR(64)  NOT NULL COMMENT '归属用户ID',
    type          VARCHAR(10)  NOT NULL COMMENT '账单类型（expense=支出 income=收入）',
    amount        BIGINT       NOT NULL COMMENT '金额（分）',
    category      VARCHAR(32)  NOT NULL COMMENT '分类名称',
    category_icon VARCHAR(32)  DEFAULT NULL COMMENT '分类图标标识',
    date          VARCHAR(10)  NOT NULL COMMENT '记账日期（YYYY-MM-DD）',
    note          VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_by     VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by     VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（1=正常 0=删除）',
    PRIMARY KEY (id),
    KEY idx_user_date (user_id, date),
    KEY idx_user_type (user_id, type)
) ENGINE = InnoDB COMMENT = '账单表';

-- ============================================================
-- 自定义分类表（预设分类不落库，见后端 PresetCategory 常量）
-- ============================================================
DROP TABLE IF EXISTS t_custom_category;
CREATE TABLE t_custom_category (
    id         VARCHAR(64) NOT NULL COMMENT '分类ID（c_开头）',
    user_id    VARCHAR(64) NOT NULL COMMENT '归属用户ID',
    type       VARCHAR(10) NOT NULL COMMENT '分类类型（expense=支出 income=收入）',
    name       VARCHAR(32) NOT NULL COMMENT '分类名称',
    icon       VARCHAR(32) NOT NULL COMMENT '图标标识',
    create_by  VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by  VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    status     TINYINT     NOT NULL DEFAULT 1 COMMENT '状态（1=正常 0=删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_type_name (user_id, type, name),
    KEY idx_user_type (user_id, type)
) ENGINE = InnoDB COMMENT = '自定义分类表';

-- ============================================================
-- 在测试库执行同样的建表结构
-- ============================================================
USE niuniu_account_test;

DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user LIKE niuniu_account.t_user;

DROP TABLE IF EXISTS t_bill;
CREATE TABLE t_bill LIKE niuniu_account.t_bill;

DROP TABLE IF EXISTS t_custom_category;
CREATE TABLE t_custom_category LIKE niuniu_account.t_custom_category;

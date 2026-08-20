# 牛牛记账后端接口文档

> 基于小程序当前代码逻辑梳理，供 SpringBoot 后端服务实现参考。
>
> **通用约定：**
> - 金额单位：**整数分**（如 `9999` = 99.99 元），避免浮点精度问题
> - 账单 ID：字符串类型，前端用 `时间戳_随机串` 生成，后端可用 UUID 或雪花 ID
> - 分类图标：字符串标识名（如 `food`、`transport`），后端返回完整名，前端自行解析为 SVG
> - 日期格式：`YYYY-MM-DD`，月份格式：`YYYY-MM`
> - 认证方式：请求头 `Authorization: Bearer <token>`

---

## 一、用户模块

### 1.1 微信登录

小程序 `login.js` 中用户填写昵称、选择头像后调用登录。

> **对应前端代码：** `pages/login/login.js → handleLogin()`

```
POST /api/user/login
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | String | 是 | `wx.login()` 获取的临时登录凭证 |
| nickName | String | 是 | 用户昵称（前端已校验非空） |
| avatarUrl | String | 否 | 头像路径（用户目录本地路径或网络 URL） |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": "u_1689a3f2",
    "nickName": "小明",
    "avatarUrl": "https://cdn.example.com/avatar/u_1689a3f2.png"
  }
}
```

---

### 1.2 获取用户信息

小程序 `profile.js` 进入页面时加载用户信息。

> **对应前端代码：** `pages/profile/profile.js → onShow()`

```
GET /api/user/info
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "userId": "u_1689a3f2",
    "nickName": "小明",
    "avatarUrl": "https://cdn.example.com/avatar/u_1689a3f2.png",
    "loginTime": 1689897600000
  }
}
```

---

### 1.3 更新用户信息

用户修改昵称或头像时调用。

> **对应前端代码：** `pages/profile/profile.js`（当前直接本地保存，后端化后需调此接口）

```
PUT /api/user/info
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickName | String | 否 | 新昵称 |
| avatarUrl | String | 否 | 新头像 URL |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "nickName": "小明",
    "avatarUrl": "https://cdn.example.com/avatar/u_1689a3f2.png"
  }
}
```

---

### 1.4 退出登录

小程序 `profile.js` 中用户确认退出后清除本地数据。

> **对应前端代码：** `pages/profile/profile.js → handleLogout()`
>
> **注意：** 后端化后退出不再清除数据，数据保留在服务端，重新登录可恢复。

```
POST /api/user/logout
```

**响应示例**

```json
{
  "code": 200,
  "message": "已退出登录"
}
```

---

## 二、账单模块

### 2.1 新建账单

小程序 `add-bill.js` 中用户填写金额、分类、日期、备注后点击保存。

> **对应前端代码：** `pages/add-bill/add-bill.js → handleSave()`（新建分支）
>
> **金额校验：** 整数部分最多 10 位，小数部分最多 2 位，最大 `9999999999.99`

```
POST /api/bill
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 是 | 账单类型：`expense` 支出 / `income` 收入 |
| amount | Integer | 是 | 金额（分），如 9999 表示 99.99 元 |
| category | String | 是 | 分类名称，如 `餐饮` |
| categoryIcon | String | 是 | 分类图标标识，如 `food` |
| date | String | 是 | 记账日期 `YYYY-MM-DD` |
| note | String | 否 | 备注，可为空 |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "id": "b_1689897600_abc123",
    "type": "expense",
    "amount": 9999,
    "category": "餐饮",
    "categoryIcon": "food",
    "date": "2026-08-20",
    "note": "午饭",
    "createTime": 1689897600000
  }
}
```

---

### 2.2 修改账单

小程序 `add-bill.js` 中用户从总览页点击账单进入编辑模式后保存。

> **对应前端代码：** `pages/add-bill/add-bill.js → handleSave()`（编辑分支）
>
> 路径参数 `id` 即前端 `editId`，通过 `globalData.editBillId` 传递

```
PUT /api/bill/{id}
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 否 | 账单类型 |
| amount | Integer | 否 | 金额（分） |
| category | String | 否 | 分类名称 |
| categoryIcon | String | 否 | 分类图标标识 |
| date | String | 否 | 记账日期 |
| note | String | 否 | 备注 |

**响应示例**

```json
{
  "code": 200,
  "message": "修改成功"
}
```

---

### 2.3 删除账单

小程序 `dashboard.js` 中用户左滑账单条目后点击红色"删除"按钮，弹出二次确认框后删除。

> **对应前端代码：** `pages/dashboard/dashboard.js → handleDeleteBill()`
>
> 前端通过 `wx.showModal` 实现二次确认

```
DELETE /api/bill/{id}
```

**响应示例**

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

### 2.4 获取单条账单详情

小程序 `add-bill.js` 中用户从总览页点击账单进入编辑模式时加载。

> **对应前端代码：** `pages/add-bill/add-bill.js → loadForEdit()` → `storage.getBillById()`

```
GET /api/bill/{id}
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "id": "b_1689897600_abc123",
    "type": "expense",
    "amount": 9999,
    "category": "餐饮",
    "categoryIcon": "food",
    "date": "2026-08-20",
    "note": "午饭",
    "createTime": 1689897600000
  }
}
```

---

### 2.5 获取月度账单列表

小程序 `dashboard.js` 进入页面或切换月份时加载，按日期分组展示。

> **对应前端代码：** `pages/dashboard/dashboard.js → loadData()` → `storage.getBillsByMonth()`
>
> 前端在收到平铺列表后按日期分组、组内按 createTime 倒序

```
GET /api/bill/month/{month}
```

**路径参数**

| 参数 | 说明 |
|------|------|
| month | 月份 `YYYY-MM`，如 `2026-08` |

**响应示例**

```json
{
  "code": 200,
  "data": [
    {
      "id": "b_1689897600_abc123",
      "type": "expense",
      "amount": 9999,
      "category": "餐饮",
      "categoryIcon": "food",
      "date": "2026-08-20",
      "note": "午饭",
      "createTime": 1689897600000
    },
    {
      "id": "b_1689800000_def456",
      "type": "income",
      "amount": 500000,
      "category": "工资",
      "categoryIcon": "salary",
      "date": "2026-08-20",
      "note": "",
      "createTime": 1689800000000
    }
  ]
}
```

---

### 2.6 获取月度汇总

小程序 `dashboard.js` 加载月度数据时计算总支出、总收入、结余。

> **对应前端代码：** `pages/dashboard/dashboard.js → loadData()` → `util.calculateSummary()`

可合并到 **2.5 月度账单列表** 的响应中返回，也可独立接口：

```
GET /api/bill/summary?month={month}
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| month | String | 是 | 月份 `YYYY-MM` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "totalExpense": 15000,
    "totalIncome": 50000,
    "balance": 35000
  }
}
```

---

### 2.7 获取日期范围内账单列表

小程序 `statistics.js` 按日期范围 + 类型筛选时加载。

> **对应前端代码：** `pages/statistics/statistics.js → computeStats()` → `storage.getBillsByDateRange()`

```
GET /api/bill/range?startDate={startDate}&endDate={endDate}
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 是 | 开始日期 `YYYY-MM-DD` |
| endDate | String | 是 | 结束日期 `YYYY-MM-DD` |

**响应示例**

```json
{
  "code": 200,
  "data": [
    {
      "id": "b_1689897600_abc123",
      "type": "expense",
      "amount": 9999,
      "category": "餐饮",
      "categoryIcon": "food",
      "date": "2026-08-20",
      "note": "午饭",
      "createTime": 1689897600000
    }
  ]
}
```

---

### 2.8 导出账单数据

小程序 `profile.js` 中用户点击"导出数据"后生成 JSON 并复制到剪贴板。

> **对应前端代码：** `pages/profile/profile.js → handleExport()`
>
> 前端导出格式：`{ app, version, exportTime, bills: [...] }`

```
GET /api/bill/export
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "app": "niuniu-account",
    "version": "1.0.0",
    "exportTime": 1689897600000,
    "bills": [
      {
        "id": "b_1689897600_abc123",
        "type": "expense",
        "amount": 9999,
        "category": "餐饮",
        "categoryIcon": "food",
        "date": "2026-08-20",
        "note": "午饭",
        "createTime": 1689897600000
      }
    ]
  }
}
```

---

### 2.9 导入账单数据

小程序 `profile.js` 中用户选择 JSON 文件后解析并导入，按 ID 去重。

> **对应前端代码：** `pages/profile/profile.js → handleImport() → parseAndImport()` → `storage.importBills()`
>
> 前端导入校验逻辑：
> - 兼容 `{ bills: [...] }` 对象格式和纯数组格式
> - 字段完整性校验：`id`、`type`、`amount`、`category`、`date` 必须存在
> - 按 ID 去重，已存在的跳过
> - `type` 值非 `income` 一律归为 `expense`

```
POST /api/bill/import
```

**请求体**（JSON 数组）

```json
[
  {
    "id": "b_1689897600_abc123",
    "type": "expense",
    "amount": 9999,
    "category": "餐饮",
    "categoryIcon": "food",
    "date": "2026-08-20",
    "note": "午饭",
    "createTime": 1689897600000
  }
]
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "importedCount": 5,
    "skippedCount": 1
  }
}
```

---

## 三、分类模块

### 3.1 获取分类列表

小程序 `add-bill.js` 加载分类网格时调用，返回系统预设 + 用户自定义分类。

> **对应前端代码：** `pages/add-bill/add-bill.js → loadCategories()` → `category.getAllCategories(type)`

```
GET /api/category/list?type={type}
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 是 | 分类类型：`expense` 支出 / `income` 收入 |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "preset": [
      { "name": "餐饮", "icon": "food" },
      { "name": "交通", "icon": "transport" },
      { "name": "购物", "icon": "shopping" },
      { "name": "娱乐", "icon": "entertainment" },
      { "name": "住房", "icon": "housing" },
      { "name": "医疗", "icon": "medical" },
      { "name": "教育", "icon": "education" },
      { "name": "其他", "icon": "other" }
    ],
    "custom": [
      { "id": "c_1689897600_xyz", "name": "宠物", "icon": "pet" }
    ]
  }
}
```

> **支出预设分类（8 项）：** 餐饮(food)、交通(transport)、购物(shopping)、娱乐(entertainment)、住房(housing)、医疗(medical)、教育(education)、其他(other)
>
> **收入预设分类（5 项）：** 工资(salary)、兼职(partTime)、理财(investment)、红包(redEnvelope)、其他(other)
>
> 对应前端代码：`utils/category.js → DEFAULT_EXPENSE_CATEGORIES / DEFAULT_INCOME_CATEGORIES`

---

### 3.2 新增自定义分类

小程序 `category-manage.js` 中用户新增分类时调用，含重名校验。

> **对应前端代码：** `pages/category-manage/category-manage.js → confirmDialog()`（新增分支）
>
> 前端重名校验逻辑：同类型下与所有分类（预设 + 自定义）不重复

```
POST /api/category
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 是 | 分类类型：`expense` / `income` |
| name | String | 是 | 分类名称（前端已校验非空 + 不重复） |
| icon | String | 是 | 图标标识，如 `pet` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "id": "c_1689897600_xyz",
    "name": "宠物",
    "icon": "pet"
  }
}
```

---

### 3.3 修改自定义分类

小程序 `category-manage.js` 中用户编辑分类时调用。

> **对应前端代码：** `pages/category-manage/category-manage.js → confirmDialog()`（编辑分支）

```
PUT /api/category/{id}
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 是 | 分类类型：`expense` / `income` |
| name | String | 否 | 新分类名称 |
| icon | String | 否 | 新图标标识 |

**响应示例**

```json
{
  "code": 200,
  "message": "修改成功"
}
```

---

### 3.4 删除自定义分类

小程序 `category-manage.js` 中用户删除分类时调用，含二次确认。

> **对应前端代码：** `pages/category-manage/category-manage.js → handleDelete()`
>
> 前端提示"历史账单不受影响"——分类删除后，已保存的账单仍保留原 `category` 和 `categoryIcon` 字段值

```
DELETE /api/category/{id}?type={type}
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 是 | 分类类型：`expense` / `income` |

**响应示例**

```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

## 四、统计模块

### 4.1 日期范围汇总统计

小程序 `statistics.js` 按日期范围 + 类型筛选后计算总支出、总收入。

> **对应前端代码：** `pages/statistics/statistics.js → computeStats()`
>
> 前端筛选类型：`all` 全部 / `expense` 仅支出 / `income` 仅收入

```
GET /api/statistics/summary?startDate={startDate}&endDate={endDate}&type={type}
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 是 | 开始日期 `YYYY-MM-DD` |
| endDate | String | 是 | 结束日期 `YYYY-MM-DD` |
| type | String | 否 | 筛选类型：`all`（默认）/ `expense` / `income` |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "totalExpense": 15000,
    "totalIncome": 50000,
    "balance": 35000
  }
}
```

---

### 4.2 分类占比统计

小程序 `statistics.js` 按类型+分类名分组统计金额和占比，结果按金额倒序。

> **对应前端代码：** `pages/statistics/statistics.js → computeStats()`
>
> 前端占比计算逻辑：
> - 筛选 `all`：分母 = 总支出 + 总收入
> - 筛选 `expense`：分母 = 总支出
> - 筛选 `income`：分母 = 总收入
> - 按 `type + category` 组合作为 key 去重统计

```
GET /api/statistics/category?startDate={startDate}&endDate={endDate}&type={type}
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 是 | 开始日期 `YYYY-MM-DD` |
| endDate | String | 是 | 结束日期 `YYYY-MM-DD` |
| type | String | 否 | 筛选类型：`all`（默认）/ `expense` / `income` |

**响应示例**

```json
{
  "code": 200,
  "data": [
    {
      "key": "expense_餐饮",
      "type": "expense",
      "category": "餐饮",
      "categoryIcon": "food",
      "amount": 5000,
      "percentage": 33.3
    },
    {
      "key": "expense_交通",
      "type": "expense",
      "category": "交通",
      "categoryIcon": "transport",
      "amount": 3000,
      "percentage": 20.0
    }
  ]
}
```

---

## 附录：数据模型

### 账单（Bill）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 账单唯一 ID |
| type | String | `expense` 支出 / `income` 收入 |
| amount | Integer | 金额（分） |
| category | String | 分类名称 |
| categoryIcon | String | 分类图标标识 |
| date | String | 记账日期 `YYYY-MM-DD` |
| note | String | 备注，可为空 |
| createTime | Long | 创建时间戳（毫秒） |

### 用户（User）

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | String | 用户唯一 ID |
| nickName | String | 昵称 |
| avatarUrl | String | 头像 URL |
| loginTime | Long | 登录时间戳（毫秒） |

### 自定义分类（CustomCategory）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 分类唯一 ID |
| name | String | 分类名称 |
| icon | String | 图标标识 |

---

## 附录：前端业务逻辑备注

| 业务点 | 前端处理方式 |
|--------|-------------|
| 金额输入限制 | 整数部分最多 10 位，小数部分最多 2 位，最大 `9999999999.99` 元 |
| 保存后跳转 | 新建保存 → 提示"保存成功" → 500ms 后跳转总览页；编辑保存 → 提示"修改成功" → 500ms 后跳转总览页 |
| 取消按钮逻辑 | 新建模式：清空当前页数据，留在本页；编辑模式：清空编辑状态，跳回总览页 |
| 保存后清空 | 保存成功后立即调用 `resetToNew()` 清空表单，避免页面缓存残留 |
| 左滑删除 | 总览页账单条目左滑露出红色"删除"按钮 → 点击弹出 `showModal` 二次确认 → 确认后删除并刷新列表 |
| 点击编辑 | 总览页点击账单条目 → 通过 `globalData.editBillId` 传递 ID → 跳转记一笔页面进入编辑模式 |
| 分类重名校验 | 同类型下（预设+自定义）名称不重复，编辑时排除自身 |
| 删除分类 | 二次确认，提示"历史账单不受影响"——已保存账单保留原 `category` 和 `categoryIcon` 字段值 |
| 导出格式 | `{ app, version, exportTime, bills: [...] }` |
| 导入校验 | 兼容对象和数组格式；按 ID 去重；必填字段校验（id、type、amount、category、date） |
| 月份分组 | 前端按 `date` 字段分组，组间按日期倒序，组内按 `createTime` 倒序 |
| 金额显示 | 前端格式化为带千分位的 `X,XXX.XX` 格式，支出前缀 `-`，收入前缀 `+` |

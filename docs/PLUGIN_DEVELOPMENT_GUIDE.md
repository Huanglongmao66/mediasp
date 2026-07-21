# 订阅源插件开发规范

## 概述

MPVP 插件系统允许开发者通过 JSON 配置文件定义订阅源，无需编写代码即可添加新的内容数据源。插件支持多种数据源格式，包括 JSON API、RSS 订阅、HTML 解析和自定义脚本。

## 插件结构

### 完整插件配置示例

```json
{
  "meta": {
    "id": "plugin_demo_v1",
    "name": "示例插件",
    "version": "1.0.0",
    "author": "开发者名称",
    "description": "这是一个示例插件，用于演示插件系统的基本用法",
    "type": "JSON",
    "mediaType": "VIDEO",
    "icon": "https://example.com/icon.png",
    "tags": ["示例", "视频"],
    "website": "https://example.com",
    "supportUrl": "https://example.com/support"
  },
  "configParams": [
    {
      "name": "api_key",
      "displayName": "API密钥",
      "type": "STRING",
      "required": true,
      "defaultValue": "",
      "placeholder": "请输入API密钥"
    }
  ],
  "parseRule": {
    "request": {
      "urlTemplate": "https://api.example.com/videos?page={{page}}&api_key={{api_key}}",
      "method": "GET",
      "headers": {
        "User-Agent": "MPVP-Plugin/1.0"
      },
      "queryParams": [],
      "bodyParams": [],
      "contentType": "JSON",
      "timeoutSeconds": 30,
      "retryCount": 2
    },
    "response": {
      "dataPath": "data",
      "itemPath": "items",
      "fields": [
        {
          "targetField": "ID",
          "sourcePath": "id",
          "transform": "",
          "defaultValue": ""
        },
        {
          "targetField": "TITLE",
          "sourcePath": "title",
          "transform": "",
          "defaultValue": "未知标题"
        },
        {
          "targetField": "URL",
          "sourcePath": "url",
          "transform": "",
          "defaultValue": ""
        }
      ],
      "errorPath": "error",
      "errorMessagePath": "message"
    },
    "pagination": {
      "enabled": true,
      "pageParam": "page",
      "pageSizeParam": "page_size",
      "defaultPageSize": 20,
      "totalPath": "total",
      "hasMorePath": "has_more"
    }
  },
  "customScript": null,
  "exampleUrl": "https://api.example.com/videos?page=1",
  "createdAt": 1699999999999,
  "updatedAt": 1699999999999
}
```

## 字段详解

### Meta（元数据）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 是 | 插件唯一标识符，建议使用 `plugin_` 前缀 |
| name | String | 是 | 插件名称，显示给用户 |
| version | String | 否 | 版本号，默认 `1.0.0` |
| author | String | 否 | 作者名称 |
| description | String | 否 | 插件描述 |
| type | PluginType | 是 | 插件类型：JSON/RSS/HTML/CUSTOM |
| mediaType | MediaType | 是 | 媒体类型：VIDEO/MUSIC/IMAGE/NOVEL/RADIO |
| icon | String | 否 | 插件图标URL |
| tags | List<String> | 否 | 标签列表 |
| website | String | 否 | 插件官网 |
| supportUrl | String | 否 | 支持页面 |

### ConfigParams（配置参数）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 参数名称，用于模板替换 |
| displayName | String | 是 | 显示名称 |
| type | ParamType | 是 | 参数类型：STRING/NUMBER/BOOLEAN/SELECT |
| required | Boolean | 否 | 是否必填，默认 false |
| defaultValue | String | 否 | 默认值 |
| placeholder | String | 否 | 输入框占位提示 |
| options | List<String> | 否 | 选择类型的选项列表 |

### ParseRule（解析规则）

#### Request（请求配置）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| urlTemplate | String | 是 | URL模板，支持 `{{param_name}}` 占位符 |
| method | HttpMethod | 否 | 请求方法：GET/POST，默认 GET |
| headers | Map<String,String> | 否 | 请求头 |
| queryParams | List<PluginParamMapping> | 否 | 查询参数 |
| bodyParams | List<PluginParamMapping> | 否 | 请求体参数 |
| contentType | ContentType | 否 | 内容类型：JSON/FORM/TEXT |
| timeoutSeconds | Int | 否 | 超时时间，默认 30 秒 |
| retryCount | Int | 否 | 重试次数，默认 2 次 |

#### Response（响应配置）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dataPath | String | 否 | 数据根路径，如 `data.items` |
| itemPath | String | 否 | 列表路径 |
| fields | List<PluginFieldMapping> | 是 | 字段映射列表 |
| errorPath | String | 否 | 错误字段路径 |
| errorMessagePath | String | 否 | 错误消息路径 |

#### FieldMapping（字段映射）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetField | TargetField | 是 | 目标字段 |
| sourcePath | String | 是 | 源数据路径 |
| transform | String | 否 | 转换函数：trim/lowercase/uppercase |
| defaultValue | String | 否 | 默认值 |

#### Pagination（分页配置）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| enabled | Boolean | 否 | 是否启用分页，默认 false |
| pageParam | String | 否 | 页码参数名，默认 `page` |
| pageSizeParam | String | 否 | 每页数量参数名，默认 `page_size` |
| defaultPageSize | Int | 否 | 默认每页数量，默认 20 |
| totalPath | String | 否 | 总数量路径 |
| hasMorePath | String | 否 | 是否有更多路径 |

## 支持的目标字段

| TargetField | 说明 | 适用类型 |
|-------------|------|----------|
| ID | 媒体ID | 全部 |
| TITLE | 标题 | 全部 |
| URL | 播放/下载链接 | 全部 |
| COVER_URL | 封面图片URL | VIDEO/MUSIC/IMAGE/NOVEL/RADIO |
| DURATION | 时长（毫秒） | VIDEO/MUSIC/RADIO |
| SIZE | 文件大小（字节） | VIDEO/MUSIC/IMAGE |
| DESCRIPTION | 描述 | 全部 |
| AUTHOR | 作者 | 全部 |
| PUBLISH_TIME | 发布时间 | 全部 |
| VIEW_COUNT | 播放/浏览量 | VIDEO/MUSIC/IMAGE/RADIO |
| CATEGORY | 分类 | 全部 |
| MIME_TYPE | MIME类型 | VIDEO/MUSIC/IMAGE |

## URL 模板语法

URL 模板支持以下占位符：

- `{{param_name}}` - 替换为配置参数值
- `{{page}}` - 自动替换为当前页码（启用分页时）
- `{{page_size}}` - 自动替换为每页数量（启用分页时）

示例：
```
https://api.example.com/videos?page={{page}}&limit={{page_size}}&api_key={{api_key}}
```

## 路径语法

路径使用点号 `.` 分隔层级，支持数组索引：

- `data.items` - 获取 data 对象下的 items 数组
- `data.items.0.title` - 获取 items 数组第一个元素的 title
- `results` - 获取根级 results 字段

## 开发流程

### 1. 确定数据源类型

根据目标数据源选择合适的插件类型：
- **JSON**: RESTful API 返回 JSON 格式
- **RSS**: RSS/Atom 订阅源
- **HTML**: 需要解析 HTML 页面
- **CUSTOM**: 需要自定义脚本处理

### 2. 分析 API 响应结构

分析目标 API 返回的数据结构，确定：
- 数据根路径
- 列表路径
- 每个字段的路径

### 3. 创建插件配置

按照上述规范创建 JSON 配置文件。

### 4. 测试插件

使用插件编辑器的测试功能验证插件是否正常工作。

### 5. 导入/导出

- 通过插件管理界面导入 JSON 配置
- 导出插件配置分享给其他用户

## 最佳实践

1. **使用唯一 ID**: 插件 ID 应该全局唯一，建议使用 `plugin_{name}_{version}` 格式
2. **提供默认值**: 为可选字段提供合理的默认值
3. **处理错误**: 配置错误路径和错误消息路径
4. **支持分页**: 如果 API 支持分页，启用分页配置
5. **添加描述**: 提供清晰的插件描述和标签
6. **测试验证**: 在发布前充分测试插件

## 常见问题

### Q: 如何处理需要认证的 API？
A: 在请求头或查询参数中添加认证信息，将 API 密钥定义为配置参数。

### Q: 如何处理动态 URL？
A: 使用 URL 模板占位符，在配置参数中定义动态部分。

### Q: 字段值需要转换怎么办？
A: 使用 transform 字段，支持 trim、lowercase、uppercase 转换。

### Q: 如何调试插件？
A: 使用插件编辑器的 JSON 预览和测试功能，查看解析结果。
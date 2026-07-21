# 订阅源插件示例

本文档提供多种常见场景的插件配置示例，帮助开发者快速理解和使用插件系统。

## 示例 1: 基础 JSON API

### 场景描述
从一个简单的 JSON API 获取视频列表。

### API 响应示例
```json
{
  "videos": [
    {
      "id": "video_001",
      "title": "示例视频",
      "url": "https://example.com/video.mp4",
      "cover": "https://example.com/cover.jpg",
      "duration": 180000,
      "author": "作者",
      "publish_time": "2024-01-01"
    }
  ]
}
```

### 插件配置
```json
{
  "meta": {
    "id": "plugin_basic_json",
    "name": "基础 JSON API",
    "version": "1.0.0",
    "description": "从基础 JSON API 获取视频列表",
    "type": "JSON",
    "mediaType": "VIDEO"
  },
  "configParams": [],
  "parseRule": {
    "request": {
      "urlTemplate": "https://api.example.com/videos",
      "method": "GET"
    },
    "response": {
      "dataPath": "",
      "itemPath": "videos",
      "fields": [
        {"targetField": "ID", "sourcePath": "id"},
        {"targetField": "TITLE", "sourcePath": "title"},
        {"targetField": "URL", "sourcePath": "url"},
        {"targetField": "COVER_URL", "sourcePath": "cover"},
        {"targetField": "DURATION", "sourcePath": "duration"},
        {"targetField": "AUTHOR", "sourcePath": "author"},
        {"targetField": "PUBLISH_TIME", "sourcePath": "publish_time"}
      ]
    }
  }
}
```

---

## 示例 2: 带认证的 API

### 场景描述
需要 API 密钥认证的 JSON API。

### 插件配置
```json
{
  "meta": {
    "id": "plugin_auth_api",
    "name": "带认证的 API",
    "version": "1.0.0",
    "description": "使用 API 密钥访问受保护的 API",
    "type": "JSON",
    "mediaType": "VIDEO"
  },
  "configParams": [
    {
      "name": "api_key",
      "displayName": "API 密钥",
      "type": "STRING",
      "required": true,
      "placeholder": "请输入 API 密钥"
    }
  ],
  "parseRule": {
    "request": {
      "urlTemplate": "https://api.example.com/videos?api_key={{api_key}}",
      "method": "GET",
      "headers": {
        "Authorization": "Bearer {{api_key}}"
      }
    },
    "response": {
      "itemPath": "data.results",
      "fields": [
        {"targetField": "ID", "sourcePath": "id"},
        {"targetField": "TITLE", "sourcePath": "title"},
        {"targetField": "URL", "sourcePath": "url"}
      ]
    }
  }
}
```

---

## 示例 3: RSS 订阅源

### 场景描述
从标准 RSS 订阅源获取内容。

### RSS 源示例
```xml
<rss version="2.0">
  <channel>
    <item>
      <title>视频标题</title>
      <link>https://example.com/video</link>
      <description>视频描述</description>
      <pubDate>Mon, 01 Jan 2024 00:00:00 UTC</pubDate>
      <enclosure url="https://example.com/video.mp4" type="video/mp4" length="1000000"/>
    </item>
  </channel>
</rss>
```

### 插件配置
```json
{
  "meta": {
    "id": "plugin_rss_feed",
    "name": "RSS 订阅源",
    "version": "1.0.0",
    "description": "从 RSS 订阅源获取视频内容",
    "type": "RSS",
    "mediaType": "VIDEO"
  },
  "configParams": [],
  "parseRule": {
    "request": {
      "urlTemplate": "https://example.com/feed.rss",
      "method": "GET"
    },
    "response": {
      "itemPath": "",
      "fields": [
        {"targetField": "ID", "sourcePath": "guid"},
        {"targetField": "TITLE", "sourcePath": "title"},
        {"targetField": "URL", "sourcePath": "enclosure/@url"},
        {"targetField": "DESCRIPTION", "sourcePath": "description"},
        {"targetField": "PUBLISH_TIME", "sourcePath": "pubDate"}
      ]
    }
  }
}
```

---

## 示例 4: 带分页的 API

### 场景描述
支持分页的 API，返回总页数和当前页数据。

### API 响应示例
```json
{
  "data": {
    "items": [...],
    "total": 100,
    "page": 1,
    "page_size": 20
  }
}
```

### 插件配置
```json
{
  "meta": {
    "id": "plugin_pagination",
    "name": "带分页的 API",
    "version": "1.0.0",
    "description": "支持分页的视频 API",
    "type": "JSON",
    "mediaType": "VIDEO"
  },
  "configParams": [],
  "parseRule": {
    "request": {
      "urlTemplate": "https://api.example.com/videos?page={{page}}&page_size={{page_size}}",
      "method": "GET"
    },
    "response": {
      "dataPath": "data",
      "itemPath": "items",
      "fields": [
        {"targetField": "ID", "sourcePath": "id"},
        {"targetField": "TITLE", "sourcePath": "title"},
        {"targetField": "URL", "sourcePath": "url"}
      ]
    },
    "pagination": {
      "enabled": true,
      "pageParam": "page",
      "pageSizeParam": "page_size",
      "defaultPageSize": 20,
      "totalPath": "data.total"
    }
  }
}
```

---

## 示例 5: 音乐订阅源

### 场景描述
从音乐 API 获取音乐列表。

### 插件配置
```json
{
  "meta": {
    "id": "plugin_music_api",
    "name": "音乐 API",
    "version": "1.0.0",
    "description": "从音乐平台获取音乐列表",
    "type": "JSON",
    "mediaType": "MUSIC"
  },
  "configParams": [],
  "parseRule": {
    "request": {
      "urlTemplate": "https://api.music.example.com/songs",
      "method": "GET"
    },
    "response": {
      "itemPath": "songs",
      "fields": [
        {"targetField": "ID", "sourcePath": "id"},
        {"targetField": "TITLE", "sourcePath": "name"},
        {"targetField": "URL", "sourcePath": "audio_url"},
        {"targetField": "COVER_URL", "sourcePath": "cover"},
        {"targetField": "DURATION", "sourcePath": "duration_ms"},
        {"targetField": "AUTHOR", "sourcePath": "artist.name"},
        {"targetField": "SIZE", "sourcePath": "size"}
      ]
    }
  }
}
```

---

## 示例 6: 图片订阅源

### 场景描述
从图片 API 获取图片列表。

### 插件配置
```json
{
  "meta": {
    "id": "plugin_image_api",
    "name": "图片 API",
    "version": "1.0.0",
    "description": "从图片平台获取图片列表",
    "type": "JSON",
    "mediaType": "IMAGE"
  },
  "configParams": [],
  "parseRule": {
    "request": {
      "urlTemplate": "https://api.images.example.com/photos",
      "method": "GET"
    },
    "response": {
      "itemPath": "photos",
      "fields": [
        {"targetField": "ID", "sourcePath": "id"},
        {"targetField": "TITLE", "sourcePath": "title"},
        {"targetField": "URL", "sourcePath": "url.full"},
        {"targetField": "COVER_URL", "sourcePath": "url.thumbnail"},
        {"targetField": "SIZE", "sourcePath": "size"},
        {"targetField": "AUTHOR", "sourcePath": "author.name"},
        {"targetField": "DESCRIPTION", "sourcePath": "description"}
      ]
    }
  }
}
```

---

## 示例 7: 小说订阅源

### 场景描述
从小说 API 获取小说列表。

### 插件配置
```json
{
  "meta": {
    "id": "plugin_novel_api",
    "name": "小说 API",
    "version": "1.0.0",
    "description": "从小说平台获取小说列表",
    "type": "JSON",
    "mediaType": "NOVEL"
  },
  "configParams": [],
  "parseRule": {
    "request": {
      "urlTemplate": "https://api.novels.example.com/books",
      "method": "GET"
    },
    "response": {
      "itemPath": "books",
      "fields": [
        {"targetField": "ID", "sourcePath": "id"},
        {"targetField": "TITLE", "sourcePath": "title"},
        {"targetField": "URL", "sourcePath": "url"},
        {"targetField": "COVER_URL", "sourcePath": "cover"},
        {"targetField": "AUTHOR", "sourcePath": "author"},
        {"targetField": "DESCRIPTION", "sourcePath": "summary"},
        {"targetField": "CATEGORY", "sourcePath": "category"}
      ]
    }
  }
}
```

---

## 示例 8: 电台订阅源

### 场景描述
从电台 API 获取电台列表。

### 插件配置
```json
{
  "meta": {
    "id": "plugin_radio_api",
    "name": "电台 API",
    "version": "1.0.0",
    "description": "从电台平台获取电台列表",
    "type": "JSON",
    "mediaType": "RADIO"
  },
  "configParams": [],
  "parseRule": {
    "request": {
      "urlTemplate": "https://api.radio.example.com/stations",
      "method": "GET"
    },
    "response": {
      "itemPath": "stations",
      "fields": [
        {"targetField": "ID", "sourcePath": "id"},
        {"targetField": "TITLE", "sourcePath": "name"},
        {"targetField": "URL", "sourcePath": "stream_url"},
        {"targetField": "COVER_URL", "sourcePath": "logo"},
        {"targetField": "AUTHOR", "sourcePath": "owner"},
        {"targetField": "DESCRIPTION", "sourcePath": "description"},
        {"targetField": "CATEGORY", "sourcePath": "genre"}
      ]
    }
  }
}
```

---

## 示例 9: POST 请求 API

### 场景描述
使用 POST 方法提交查询参数的 API。

### 插件配置
```json
{
  "meta": {
    "id": "plugin_post_api",
    "name": "POST 请求 API",
    "version": "1.0.0",
    "description": "使用 POST 方法获取数据",
    "type": "JSON",
    "mediaType": "VIDEO"
  },
  "configParams": [],
  "parseRule": {
    "request": {
      "urlTemplate": "https://api.example.com/search",
      "method": "POST",
      "contentType": "JSON",
      "bodyParams": [
        {"key": "query", "value": "video", "isDynamic": false},
        {"key": "page", "value": "{{page}}", "isDynamic": true},
        {"key": "limit", "value": "{{page_size}}", "isDynamic": true}
      ]
    },
    "response": {
      "itemPath": "results",
      "fields": [
        {"targetField": "ID", "sourcePath": "id"},
        {"targetField": "TITLE", "sourcePath": "title"},
        {"targetField": "URL", "sourcePath": "url"}
      ]
    },
    "pagination": {
      "enabled": true,
      "defaultPageSize": 20
    }
  }
}
```

---

## 示例 10: 多层嵌套数据

### 场景描述
API 返回多层嵌套的数据结构。

### API 响应示例
```json
{
  "response": {
    "code": 200,
    "message": "success",
    "result": {
      "list": [...],
      "pagination": {
        "total": 100
      }
    }
  }
}
```

### 插件配置
```json
{
  "meta": {
    "id": "plugin_nested_data",
    "name": "多层嵌套数据",
    "version": "1.0.0",
    "description": "处理多层嵌套的 API 响应",
    "type": "JSON",
    "mediaType": "VIDEO"
  },
  "configParams": [],
  "parseRule": {
    "request": {
      "urlTemplate": "https://api.example.com/v2/videos",
      "method": "GET"
    },
    "response": {
      "dataPath": "response.result",
      "itemPath": "list",
      "fields": [
        {"targetField": "ID", "sourcePath": "video.id"},
        {"targetField": "TITLE", "sourcePath": "video.title"},
        {"targetField": "URL", "sourcePath": "video.play_url"},
        {"targetField": "COVER_URL", "sourcePath": "video.cover.url"},
        {"targetField": "AUTHOR", "sourcePath": "video.author.name"}
      ]
    },
    "pagination": {
      "enabled": true,
      "totalPath": "response.result.pagination.total"
    }
  }
}
```

---

## 快速参考

### 媒体类型对应关系

| MediaType | 说明 | 必需字段 |
|-----------|------|----------|
| VIDEO | 视频 | ID, TITLE, URL |
| MUSIC | 音乐 | ID, TITLE, URL |
| IMAGE | 图片 | ID, TITLE, URL |
| NOVEL | 小说 | ID, TITLE, URL |
| RADIO | 电台 | ID, TITLE, URL |

### 路径语法示例

| 路径 | 说明 |
|------|------|
| `items` | 根级 items 数组 |
| `data.items` | data 对象下的 items 数组 |
| `items.0.title` | items 第一个元素的 title |
| `video.author.name` | 嵌套路径 |

### 转换函数

| 函数 | 说明 |
|------|------|
| `trim` | 去除首尾空格 |
| `lowercase` | 转小写 |
| `uppercase` | 转大写 |
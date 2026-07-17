(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['MultiPlatformVideoPlayer:shared'] = factory(typeof this['MultiPlatformVideoPlayer:shared'] === 'undefined' ? {} : this['MultiPlatformVideoPlayer:shared']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  return _;
}));

//# sourceMappingURL=MultiPlatformVideoPlayer-shared.js.map

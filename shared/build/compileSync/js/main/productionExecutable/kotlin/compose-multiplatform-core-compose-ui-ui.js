(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports'], factory);
  else if (typeof exports === 'object')
    factory(module.exports);
  else
    root['compose-multiplatform-core-compose-ui-ui'] = factory(typeof this['compose-multiplatform-core-compose-ui-ui'] === 'undefined' ? {} : this['compose-multiplatform-core-compose-ui-ui']);
}(this, function (_) {
  'use strict';
  //region block: pre-declaration
  //endregion
  var DefaultCacheSize;
  var buttonsFlags;
  var defaultCanvasElementId;
  //region block: init
  DefaultCacheSize = 8;
  buttonsFlags = 0;
  defaultCanvasElementId = 'ComposeTarget';
  //endregion
  return _;
}));

//# sourceMappingURL=compose-multiplatform-core-compose-ui-ui.js.map

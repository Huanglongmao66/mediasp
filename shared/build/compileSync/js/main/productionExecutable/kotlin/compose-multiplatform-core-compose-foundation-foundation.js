(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', './kotlin-kotlin-stdlib.js'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('./kotlin-kotlin-stdlib.js'));
  else {
    if (typeof this['kotlin-kotlin-stdlib'] === 'undefined') {
      throw new Error("Error loading module 'compose-multiplatform-core-compose-foundation-foundation'. Its dependency 'kotlin-kotlin-stdlib' was not found. Please, check whether 'kotlin-kotlin-stdlib' is loaded prior to 'compose-multiplatform-core-compose-foundation-foundation'.");
    }
    root['compose-multiplatform-core-compose-foundation-foundation'] = factory(typeof this['compose-multiplatform-core-compose-foundation-foundation'] === 'undefined' ? {} : this['compose-multiplatform-core-compose-foundation-foundation'], this['kotlin-kotlin-stdlib']);
  }
}(this, function (_, kotlin_kotlin) {
  'use strict';
  //region block: imports
  var Long = kotlin_kotlin.$_$.a;
  //endregion
  //region block: pre-declaration
  //endregion
  var SNAPSHOTS_INTERVAL_MILLIS;
  var TapIndicationDelay;
  var isInTouchMode;
  //region block: init
  SNAPSHOTS_INTERVAL_MILLIS = 5000;
  TapIndicationDelay = new Long(0, 0);
  isInTouchMode = false;
  //endregion
  return _;
}));

//# sourceMappingURL=compose-multiplatform-core-compose-foundation-foundation.js.map

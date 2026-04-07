$gwx2_XC_0 = (function (
  _,
  _v,
  _n,
  _p,
  _s,
  _wp,
  _wl,
  $gwn,
  $gwl,
  $gwh,
  wh,
  $gstack,
  $gwrt,
  gra,
  grb,
  TestTest,
  wfor,
  _ca,
  _da,
  _r,
  _rz,
  _o,
  _oz,
  _1,
  _1z,
  _2,
  _2z,
  _m,
  _mz,
  nv_getDate,
  nv_getRegExp,
  nv_console,
  nv_parseInt,
  nv_parseFloat,
  nv_isNaN,
  nv_isFinite,
  nv_decodeURI,
  nv_decodeURIComponent,
  nv_encodeURI,
  nv_encodeURIComponent,
  $gdc,
  nv_JSON,
  _af,
  _gv,
  _ai,
  _grp,
  _gd,
  _gapi,
  $ixc,
  _ic,
  _w,
  _ev,
  _tsd
) {
  return function (path, global) {
    if (typeof global === "undefined") {
      if (typeof __GWX_GLOBAL__ === "undefined") global = {};
      else global = __GWX_GLOBAL__;
    }
    if (typeof __WXML_GLOBAL__ === "undefined") {
      __WXML_GLOBAL__ = {};
    }
    __WXML_GLOBAL__.modules = __WXML_GLOBAL__.modules || {};
    var e_ = {};
    if (typeof global.entrys === "undefined") global.entrys = {};
    e_ = global.entrys;
    var d_ = {};
    if (typeof global.defines === "undefined") global.defines = {};
    d_ = global.defines;
    var f_ = {};
    if (typeof global.modules === "undefined") global.modules = {};
    f_ = global.modules || {};
    var p_ = {};
    __WXML_GLOBAL__.ops_cached = __WXML_GLOBAL__.ops_cached || {};
    __WXML_GLOBAL__.ops_set = __WXML_GLOBAL__.ops_set || {};
    __WXML_GLOBAL__.ops_init = __WXML_GLOBAL__.ops_init || {};
    var z = __WXML_GLOBAL__.ops_set.$gwx2_XC_0 || [];
    function gz$gwx2_XC_0_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx2_XC_0_1)
        return __WXML_GLOBAL__.ops_cached.$gwx2_XC_0_1;
      __WXML_GLOBAL__.ops_cached.$gwx2_XC_0_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([3, "battery-settings-page data-v-7f55b84c"]);
        Z([3, "page-header data-v-7f55b84c"]);
        Z([3, "header-title data-v-7f55b84c"]);
        Z([3, "电池参数设置"]);
        Z([3, "tab-bar data-v-7f55b84c"]);
        Z([3, "__e"]);
        Z([
          [4],
          [
            [5],
            [[5], [[5], [1, "tab-item"]], [1, "data-v-7f55b84c"]],
            [
              [2, "?:"],
              [
                [2, "==="],
                [[7], [3, "currentTab"]],
                [1, 0],
              ],
              [1, "active"],
              [1, ""],
            ],
          ],
        ]);
        Z([
          [4],
          [
            [5],
            [
              [4],
              [
                [5],
                [[5], [1, "tap"]],
                [
                  [4],
                  [
                    [5],
                    [[4], [[5], [[5], [1, "switchTab"]], [[4], [[5], [1, 0]]]]],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z([3, "电池1"]);
        Z(z[5]);
        Z([
          [4],
          [
            [5],
            [[5], [[5], [1, "tab-item"]], [1, "data-v-7f55b84c"]],
            [
              [2, "?:"],
              [
                [2, "==="],
                [[7], [3, "currentTab"]],
                [1, 1],
              ],
              [1, "active"],
              [1, ""],
            ],
          ],
        ]);
        Z([
          [4],
          [
            [5],
            [
              [4],
              [
                [5],
                [[5], [1, "tap"]],
                [
                  [4],
                  [
                    [5],
                    [[4], [[5], [[5], [1, "switchTab"]], [[4], [[5], [1, 1]]]]],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z([3, "电池2"]);
        Z([
          [2, "==="],
          [[7], [3, "currentTab"]],
          [1, 0],
        ]);
        Z([
          [4],
          [
            [5],
            [[5], [[5], [1, "content-panel"]], [1, "data-v-7f55b84c"]],
            [1, "battery1-bg"],
          ],
        ]);
        Z([3, "remark-container data-v-7f55b84c"]);
        Z([3, "remark-label data-v-7f55b84c"]);
        Z([3, "备注名："]);
        Z(z[5]);
        Z([3, "remark-input data-v-7f55b84c"]);
        Z([
          [4],
          [
            [5],
            [
              [4],
              [
                [5],
                [[5], [1, "input"]],
                [
                  [4],
                  [
                    [5],
                    [
                      [4],
                      [
                        [5],
                        [
                          [5],
                          [[5], [1, "__set_model"]],
                          [
                            [4],
                            [
                              [5],
                              [
                                [5],
                                [[5], [[5], [1, "$0"]], [1, "remark"]],
                                [1, "$event"],
                              ],
                              [[4], [[5]]],
                            ],
                          ],
                        ],
                        [[4], [[5], [1, "battery1"]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z([3, "请输入参数备注名"]);
        Z([[6], [[7], [3, "battery1"]], [3, "remark"]]);
        Z([3, "param-form data-v-7f55b84c"]);
        Z([3, "form-item data-v-7f55b84c"]);
        Z([3, "param-label data-v-7f55b84c"]);
        Z([3, "母线电流："]);
        Z(z[5]);
        Z(z[5]);
        Z([3, "param-input data-v-7f55b84c"]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [[5], [[5], [1, "$0"]], [1, "busCurrent"]],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery1"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z([3, "请输入母线电流"]);
        Z([3, "number"]);
        Z([[6], [[7], [3, "battery1"]], [3, "busCurrent"]]);
        Z(z[24]);
        Z(z[25]);
        Z([3, "相线电流："]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [[5], [[5], [1, "$0"]], [1, "phaseCurrent"]],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery1"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z([3, "请输入相线电流"]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery1"]], [3, "phaseCurrent"]]);
        Z(z[24]);
        Z(z[25]);
        Z([3, "弱磁电流："]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [
                                    [5],
                                    [[5], [1, "$0"]],
                                    [1, "weakMagnetCurrent"],
                                  ],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery1"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z([3, "请输入弱磁电流"]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery1"]], [3, "weakMagnetCurrent"]]);
        Z(z[24]);
        Z(z[25]);
        Z([3, "过压点："]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [
                                    [5],
                                    [[5], [1, "$0"]],
                                    [1, "overVoltagePoint"],
                                  ],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery1"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z([3, "请输入过压点"]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery1"]], [3, "overVoltagePoint"]]);
        Z(z[24]);
        Z(z[25]);
        Z([3, "欠压点："]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [
                                    [5],
                                    [[5], [1, "$0"]],
                                    [1, "underVoltagePoint"],
                                  ],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery1"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z([3, "请输入欠压点"]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery1"]], [3, "underVoltagePoint"]]);
        Z(z[24]);
        Z(z[25]);
        Z([3, "降载起始电压："]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [
                                    [5],
                                    [[5], [1, "$0"]],
                                    [1, "loadReductionStartVoltage"],
                                  ],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery1"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z([3, "请输入降载起始电压"]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery1"]], [3, "loadReductionStartVoltage"]]);
        Z(z[24]);
        Z(z[25]);
        Z([3, "降载截至电压："]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [
                                    [5],
                                    [[5], [1, "$0"]],
                                    [1, "loadReductionEndVoltage"],
                                  ],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery1"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z([3, "请输入降载截至电压"]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery1"]], [3, "loadReductionEndVoltage"]]);
        Z([3, "button-group data-v-7f55b84c"]);
        Z(z[5]);
        Z([3, "btn reset-btn data-v-7f55b84c"]);
        Z([
          [4],
          [
            [5],
            [
              [4],
              [
                [5],
                [[5], [1, "tap"]],
                [
                  [4],
                  [
                    [5],
                    [
                      [4],
                      [[5], [[5], [1, "resetToDefault"]], [[4], [[5], [1, 0]]]],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z([3, "恢复默认"]);
        Z(z[5]);
        Z([3, "btn confirm-btn data-v-7f55b84c"]);
        Z([
          [4],
          [
            [5],
            [
              [4],
              [
                [5],
                [[5], [1, "tap"]],
                [
                  [4],
                  [
                    [5],
                    [
                      [4],
                      [
                        [5],
                        [[5], [1, "confirmSettings"]],
                        [[4], [[5], [1, 0]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z([3, "确认"]);
        Z(z[5]);
        Z([3, "btn cancel-btn data-v-7f55b84c"]);
        Z([
          [4],
          [
            [5],
            [
              [4],
              [
                [5],
                [[5], [1, "tap"]],
                [
                  [4],
                  [
                    [5],
                    [
                      [4],
                      [[5], [[5], [1, "cancel"]], [[4], [[5], [1, "$event"]]]],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z([3, "取消"]);
        Z([
          [2, "==="],
          [[7], [3, "currentTab"]],
          [1, 1],
        ]);
        Z([
          [4],
          [
            [5],
            [[5], [[5], [1, "content-panel"]], [1, "data-v-7f55b84c"]],
            [1, "battery2-bg"],
          ],
        ]);
        Z(z[15]);
        Z(z[16]);
        Z(z[17]);
        Z(z[5]);
        Z(z[19]);
        Z([
          [4],
          [
            [5],
            [
              [4],
              [
                [5],
                [[5], [1, "input"]],
                [
                  [4],
                  [
                    [5],
                    [
                      [4],
                      [
                        [5],
                        [
                          [5],
                          [[5], [1, "__set_model"]],
                          [
                            [4],
                            [
                              [5],
                              [
                                [5],
                                [[5], [[5], [1, "$0"]], [1, "remark"]],
                                [1, "$event"],
                              ],
                              [[4], [[5]]],
                            ],
                          ],
                        ],
                        [[4], [[5], [1, "battery2"]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z(z[21]);
        Z([[6], [[7], [3, "battery2"]], [3, "remark"]]);
        Z(z[23]);
        Z(z[24]);
        Z(z[25]);
        Z(z[26]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [[5], [[5], [1, "$0"]], [1, "busCurrent"]],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery2"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z(z[31]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery2"]], [3, "busCurrent"]]);
        Z(z[24]);
        Z(z[25]);
        Z(z[36]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [[5], [[5], [1, "$0"]], [1, "phaseCurrent"]],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery2"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z(z[41]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery2"]], [3, "phaseCurrent"]]);
        Z(z[24]);
        Z(z[25]);
        Z(z[46]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [
                                    [5],
                                    [[5], [1, "$0"]],
                                    [1, "weakMagnetCurrent"],
                                  ],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery2"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z(z[51]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery2"]], [3, "weakMagnetCurrent"]]);
        Z(z[24]);
        Z(z[25]);
        Z(z[56]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [
                                    [5],
                                    [[5], [1, "$0"]],
                                    [1, "overVoltagePoint"],
                                  ],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery2"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z(z[61]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery2"]], [3, "overVoltagePoint"]]);
        Z(z[24]);
        Z(z[25]);
        Z(z[66]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [
                                    [5],
                                    [[5], [1, "$0"]],
                                    [1, "underVoltagePoint"],
                                  ],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery2"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z(z[71]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery2"]], [3, "underVoltagePoint"]]);
        Z(z[24]);
        Z(z[25]);
        Z(z[76]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [
                                    [5],
                                    [[5], [1, "$0"]],
                                    [1, "loadReductionStartVoltage"],
                                  ],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery2"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z(z[81]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery2"]], [3, "loadReductionStartVoltage"]]);
        Z(z[24]);
        Z(z[25]);
        Z(z[86]);
        Z(z[5]);
        Z(z[5]);
        Z(z[29]);
        Z([
          [4],
          [
            [5],
            [
              [5],
              [
                [4],
                [
                  [5],
                  [[5], [1, "input"]],
                  [
                    [4],
                    [
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [
                            [5],
                            [[5], [1, "__set_model"]],
                            [
                              [4],
                              [
                                [5],
                                [
                                  [5],
                                  [
                                    [5],
                                    [[5], [1, "$0"]],
                                    [1, "loadReductionEndVoltage"],
                                  ],
                                  [1, "$event"],
                                ],
                                [[4], [[5], [1, "number"]]],
                              ],
                            ],
                          ],
                          [[4], [[5], [1, "battery2"]]],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
            [
              [4],
              [
                [5],
                [[5], [1, "blur"]],
                [[4], [[5], [[4], [[5], [1, "$forceUpdate"]]]]],
              ],
            ],
          ],
        ]);
        Z(z[91]);
        Z(z[32]);
        Z([[6], [[7], [3, "battery2"]], [3, "loadReductionEndVoltage"]]);
        Z(z[94]);
        Z(z[5]);
        Z(z[96]);
        Z([
          [4],
          [
            [5],
            [
              [4],
              [
                [5],
                [[5], [1, "tap"]],
                [
                  [4],
                  [
                    [5],
                    [
                      [4],
                      [[5], [[5], [1, "resetToDefault"]], [[4], [[5], [1, 1]]]],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z(z[98]);
        Z(z[5]);
        Z(z[100]);
        Z([
          [4],
          [
            [5],
            [
              [4],
              [
                [5],
                [[5], [1, "tap"]],
                [
                  [4],
                  [
                    [5],
                    [
                      [4],
                      [
                        [5],
                        [[5], [1, "confirmSettings"]],
                        [[4], [[5], [1, 1]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z(z[102]);
        Z(z[5]);
        Z(z[104]);
        Z(z[105]);
        Z(z[106]);
      })(__WXML_GLOBAL__.ops_cached.$gwx2_XC_0_1);
      return __WXML_GLOBAL__.ops_cached.$gwx2_XC_0_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx2_XC_0 = z;
    __WXML_GLOBAL__.ops_init.$gwx2_XC_0 = true;
    var x = ["./subpackages/info/battery/battery.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx2_XC_0_1();
      var oB = _n("view");
      _rz(z, oB, "class", 0, e, s, gg);
      var fE = _n("view");
      _rz(z, fE, "class", 1, e, s, gg);
      var cF = _n("text");
      _rz(z, cF, "class", 2, e, s, gg);
      var hG = _oz(z, 3, e, s, gg);
      _(cF, hG);
      _(fE, cF);
      _(oB, fE);
      var oH = _n("view");
      _rz(z, oH, "class", 4, e, s, gg);
      var cI = _mz(
        z,
        "view",
        ["bindtap", 5, "class", 1, "data-event-opts", 2],
        [],
        e,
        s,
        gg
      );
      var oJ = _oz(z, 8, e, s, gg);
      _(cI, oJ);
      _(oH, cI);
      var lK = _mz(
        z,
        "view",
        ["bindtap", 9, "class", 1, "data-event-opts", 2],
        [],
        e,
        s,
        gg
      );
      var aL = _oz(z, 12, e, s, gg);
      _(lK, aL);
      _(oH, lK);
      _(oB, oH);
      var xC = _v();
      _(oB, xC);
      if (_oz(z, 13, e, s, gg)) {
        xC.wxVkey = 1;
        var tM = _n("view");
        _rz(z, tM, "class", 14, e, s, gg);
        var eN = _n("view");
        _rz(z, eN, "class", 15, e, s, gg);
        var bO = _n("text");
        _rz(z, bO, "class", 16, e, s, gg);
        var oP = _oz(z, 17, e, s, gg);
        _(bO, oP);
        _(eN, bO);
        var xQ = _mz(
          z,
          "input",
          [
            "bindinput",
            18,
            "class",
            1,
            "data-event-opts",
            2,
            "placeholder",
            3,
            "value",
            4,
          ],
          [],
          e,
          s,
          gg
        );
        _(eN, xQ);
        _(tM, eN);
        var oR = _n("form");
        _rz(z, oR, "class", 23, e, s, gg);
        var fS = _n("view");
        _rz(z, fS, "class", 24, e, s, gg);
        var cT = _n("text");
        _rz(z, cT, "class", 25, e, s, gg);
        var hU = _oz(z, 26, e, s, gg);
        _(cT, hU);
        _(fS, cT);
        var oV = _mz(
          z,
          "input",
          [
            "bindblur",
            27,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(fS, oV);
        _(oR, fS);
        var cW = _n("view");
        _rz(z, cW, "class", 34, e, s, gg);
        var oX = _n("text");
        _rz(z, oX, "class", 35, e, s, gg);
        var lY = _oz(z, 36, e, s, gg);
        _(oX, lY);
        _(cW, oX);
        var aZ = _mz(
          z,
          "input",
          [
            "bindblur",
            37,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(cW, aZ);
        _(oR, cW);
        var t1 = _n("view");
        _rz(z, t1, "class", 44, e, s, gg);
        var e2 = _n("text");
        _rz(z, e2, "class", 45, e, s, gg);
        var b3 = _oz(z, 46, e, s, gg);
        _(e2, b3);
        _(t1, e2);
        var o4 = _mz(
          z,
          "input",
          [
            "bindblur",
            47,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(t1, o4);
        _(oR, t1);
        var x5 = _n("view");
        _rz(z, x5, "class", 54, e, s, gg);
        var o6 = _n("text");
        _rz(z, o6, "class", 55, e, s, gg);
        var f7 = _oz(z, 56, e, s, gg);
        _(o6, f7);
        _(x5, o6);
        var c8 = _mz(
          z,
          "input",
          [
            "bindblur",
            57,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(x5, c8);
        _(oR, x5);
        var h9 = _n("view");
        _rz(z, h9, "class", 64, e, s, gg);
        var o0 = _n("text");
        _rz(z, o0, "class", 65, e, s, gg);
        var cAB = _oz(z, 66, e, s, gg);
        _(o0, cAB);
        _(h9, o0);
        var oBB = _mz(
          z,
          "input",
          [
            "bindblur",
            67,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(h9, oBB);
        _(oR, h9);
        var lCB = _n("view");
        _rz(z, lCB, "class", 74, e, s, gg);
        var aDB = _n("text");
        _rz(z, aDB, "class", 75, e, s, gg);
        var tEB = _oz(z, 76, e, s, gg);
        _(aDB, tEB);
        _(lCB, aDB);
        var eFB = _mz(
          z,
          "input",
          [
            "bindblur",
            77,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(lCB, eFB);
        _(oR, lCB);
        var bGB = _n("view");
        _rz(z, bGB, "class", 84, e, s, gg);
        var oHB = _n("text");
        _rz(z, oHB, "class", 85, e, s, gg);
        var xIB = _oz(z, 86, e, s, gg);
        _(oHB, xIB);
        _(bGB, oHB);
        var oJB = _mz(
          z,
          "input",
          [
            "bindblur",
            87,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(bGB, oJB);
        _(oR, bGB);
        _(tM, oR);
        var fKB = _n("view");
        _rz(z, fKB, "class", 94, e, s, gg);
        var cLB = _mz(
          z,
          "button",
          ["bindtap", 95, "class", 1, "data-event-opts", 2],
          [],
          e,
          s,
          gg
        );
        var hMB = _oz(z, 98, e, s, gg);
        _(cLB, hMB);
        _(fKB, cLB);
        var oNB = _mz(
          z,
          "button",
          ["bindtap", 99, "class", 1, "data-event-opts", 2],
          [],
          e,
          s,
          gg
        );
        var cOB = _oz(z, 102, e, s, gg);
        _(oNB, cOB);
        _(fKB, oNB);
        var oPB = _mz(
          z,
          "button",
          ["bindtap", 103, "class", 1, "data-event-opts", 2],
          [],
          e,
          s,
          gg
        );
        var lQB = _oz(z, 106, e, s, gg);
        _(oPB, lQB);
        _(fKB, oPB);
        _(tM, fKB);
        _(xC, tM);
      }
      var oD = _v();
      _(oB, oD);
      if (_oz(z, 107, e, s, gg)) {
        oD.wxVkey = 1;
        var aRB = _n("view");
        _rz(z, aRB, "class", 108, e, s, gg);
        var tSB = _n("view");
        _rz(z, tSB, "class", 109, e, s, gg);
        var eTB = _n("text");
        _rz(z, eTB, "class", 110, e, s, gg);
        var bUB = _oz(z, 111, e, s, gg);
        _(eTB, bUB);
        _(tSB, eTB);
        var oVB = _mz(
          z,
          "input",
          [
            "bindinput",
            112,
            "class",
            1,
            "data-event-opts",
            2,
            "placeholder",
            3,
            "value",
            4,
          ],
          [],
          e,
          s,
          gg
        );
        _(tSB, oVB);
        _(aRB, tSB);
        var xWB = _n("form");
        _rz(z, xWB, "class", 117, e, s, gg);
        var oXB = _n("view");
        _rz(z, oXB, "class", 118, e, s, gg);
        var fYB = _n("text");
        _rz(z, fYB, "class", 119, e, s, gg);
        var cZB = _oz(z, 120, e, s, gg);
        _(fYB, cZB);
        _(oXB, fYB);
        var h1B = _mz(
          z,
          "input",
          [
            "bindblur",
            121,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(oXB, h1B);
        _(xWB, oXB);
        var o2B = _n("view");
        _rz(z, o2B, "class", 128, e, s, gg);
        var c3B = _n("text");
        _rz(z, c3B, "class", 129, e, s, gg);
        var o4B = _oz(z, 130, e, s, gg);
        _(c3B, o4B);
        _(o2B, c3B);
        var l5B = _mz(
          z,
          "input",
          [
            "bindblur",
            131,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(o2B, l5B);
        _(xWB, o2B);
        var a6B = _n("view");
        _rz(z, a6B, "class", 138, e, s, gg);
        var t7B = _n("text");
        _rz(z, t7B, "class", 139, e, s, gg);
        var e8B = _oz(z, 140, e, s, gg);
        _(t7B, e8B);
        _(a6B, t7B);
        var b9B = _mz(
          z,
          "input",
          [
            "bindblur",
            141,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(a6B, b9B);
        _(xWB, a6B);
        var o0B = _n("view");
        _rz(z, o0B, "class", 148, e, s, gg);
        var xAC = _n("text");
        _rz(z, xAC, "class", 149, e, s, gg);
        var oBC = _oz(z, 150, e, s, gg);
        _(xAC, oBC);
        _(o0B, xAC);
        var fCC = _mz(
          z,
          "input",
          [
            "bindblur",
            151,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(o0B, fCC);
        _(xWB, o0B);
        var cDC = _n("view");
        _rz(z, cDC, "class", 158, e, s, gg);
        var hEC = _n("text");
        _rz(z, hEC, "class", 159, e, s, gg);
        var oFC = _oz(z, 160, e, s, gg);
        _(hEC, oFC);
        _(cDC, hEC);
        var cGC = _mz(
          z,
          "input",
          [
            "bindblur",
            161,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(cDC, cGC);
        _(xWB, cDC);
        var oHC = _n("view");
        _rz(z, oHC, "class", 168, e, s, gg);
        var lIC = _n("text");
        _rz(z, lIC, "class", 169, e, s, gg);
        var aJC = _oz(z, 170, e, s, gg);
        _(lIC, aJC);
        _(oHC, lIC);
        var tKC = _mz(
          z,
          "input",
          [
            "bindblur",
            171,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(oHC, tKC);
        _(xWB, oHC);
        var eLC = _n("view");
        _rz(z, eLC, "class", 178, e, s, gg);
        var bMC = _n("text");
        _rz(z, bMC, "class", 179, e, s, gg);
        var oNC = _oz(z, 180, e, s, gg);
        _(bMC, oNC);
        _(eLC, bMC);
        var xOC = _mz(
          z,
          "input",
          [
            "bindblur",
            181,
            "bindinput",
            1,
            "class",
            2,
            "data-event-opts",
            3,
            "placeholder",
            4,
            "type",
            5,
            "value",
            6,
          ],
          [],
          e,
          s,
          gg
        );
        _(eLC, xOC);
        _(xWB, eLC);
        _(aRB, xWB);
        var oPC = _n("view");
        _rz(z, oPC, "class", 188, e, s, gg);
        var fQC = _mz(
          z,
          "button",
          ["bindtap", 189, "class", 1, "data-event-opts", 2],
          [],
          e,
          s,
          gg
        );
        var cRC = _oz(z, 192, e, s, gg);
        _(fQC, cRC);
        _(oPC, fQC);
        var hSC = _mz(
          z,
          "button",
          ["bindtap", 193, "class", 1, "data-event-opts", 2],
          [],
          e,
          s,
          gg
        );
        var oTC = _oz(z, 196, e, s, gg);
        _(hSC, oTC);
        _(oPC, hSC);
        var cUC = _mz(
          z,
          "button",
          ["bindtap", 197, "class", 1, "data-event-opts", 2],
          [],
          e,
          s,
          gg
        );
        var oVC = _oz(z, 200, e, s, gg);
        _(cUC, oVC);
        _(oPC, cUC);
        _(aRB, oPC);
        _(oD, aRB);
      }
      xC.wxXCkey = 1;
      oD.wxXCkey = 1;
      _(r, oB);
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      outerGlobal.__wxml_comp_version__ = 0.02;
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx2_XC_0";
        var main = e_[path].f;
        if (typeof global === "undefined") global = {};
        global.f = $gdc(f_[path], "", 1);
        if (
          typeof outerGlobal.__webview_engine_version__ != "undefined" &&
          outerGlobal.__webview_engine_version__ + 1e-6 >= 0.02 + 1e-6 &&
          outerGlobal.__mergeData__
        ) {
          env = outerGlobal.__mergeData__(env, dd);
        }
        try {
          main(env, {}, root, global);
          _tsd(root);
          if (
            typeof outerGlobal.__webview_engine_version__ == "undefined" ||
            outerGlobal.__webview_engine_version__ + 1e-6 < 0.01 + 1e-6
          ) {
            return _ev(root);
          }
        } catch (err) {
          console.log(err);
        }
        g = "";
        return root;
      };
    }
  };
})(
  __g.a,
  __g.b,
  __g.c,
  __g.d,
  __g.e,
  __g.f,
  __g.g,
  __g.h,
  __g.i,
  __g.j,
  __g.k,
  __g.l,
  __g.m,
  __g.n,
  __g.o,
  __g.p,
  __g.q,
  __g.r,
  __g.s,
  __g.t,
  __g.u,
  __g.v,
  __g.w,
  __g.x,
  __g.y,
  __g.z,
  __g.A,
  __g.B,
  __g.C,
  __g.D,
  __g.E,
  __g.F,
  __g.G,
  __g.H,
  __g.I,
  __g.J,
  __g.K,
  __g.L,
  __g.M,
  __g.N,
  __g.O,
  __g.P,
  __g.Q,
  __g.R,
  __g.S,
  __g.T,
  __g.U,
  __g.V,
  __g.W,
  __g.X,
  __g.Y,
  __g.Z,
  __g.aa
);
if (__vd_version_info__.delayedGwx || false) $gwx2_XC_0();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/info/battery/battery.wxml"] = [
    $gwx2_XC_0,
    "./subpackages/info/battery/battery.wxml",
  ];
else
  __wxAppCode__["subpackages/info/battery/battery.wxml"] = $gwx2_XC_0(
    "./subpackages/info/battery/battery.wxml"
  );

var noCss =
  typeof __vd_version_info__ !== "undefined" &&
  __vd_version_info__.noCss === true;
if (!noCss) {
  __wxAppCode__["subpackages/info/battery/battery.wxss"] = setCssToHead(
    [
      ".",
      [1],
      "remark-container.",
      [1],
      "data-v-7f55b84c{-webkit-align-items:center;align-items:center;display:-webkit-flex;display:flex;margin-bottom:",
      [0, 30],
      ";padding:0 ",
      [0, 30],
      "}\n.",
      [1],
      "remark-label.",
      [1],
      "data-v-7f55b84c{color:#333;font-size:",
      [0, 28],
      ";width:",
      [0, 140],
      "}\n.",
      [1],
      "remark-input.",
      [1],
      "data-v-7f55b84c{background-color:#fff;border:1px solid #d9d9d9;border-radius:",
      [0, 6],
      ";-webkit-flex:1;flex:1;font-size:",
      [0, 28],
      ";height:",
      [0, 70],
      ";padding:0 ",
      [0, 20],
      "}\n.",
      [1],
      "battery-settings-page.",
      [1],
      "data-v-7f55b84c{background-color:#f5f7fa;min-height:100vh}\n.",
      [1],
      "page-header.",
      [1],
      "data-v-7f55b84c{background-color:#0056b3;padding:",
      [0, 30],
      " 0;text-align:center}\n.",
      [1],
      "header-title.",
      [1],
      "data-v-7f55b84c{color:#fff;font-size:",
      [0, 36],
      ";font-weight:700}\n.",
      [1],
      "tab-bar.",
      [1],
      "data-v-7f55b84c{background-color:#fff;border-bottom:1px solid #e5e7eb;display:-webkit-flex;display:flex;-webkit-flex-direction:row;flex-direction:row}\n.",
      [1],
      "tab-item.",
      [1],
      "data-v-7f55b84c{color:#666;-webkit-flex:1;flex:1;font-size:",
      [0, 32],
      ";padding:",
      [0, 25],
      " 0;position:relative;text-align:center}\n.",
      [1],
      "tab-item.",
      [1],
      "active.",
      [1],
      "data-v-7f55b84c{color:#1677ff;font-weight:600}\n.",
      [1],
      "tab-item.",
      [1],
      "active.",
      [1],
      "data-v-7f55b84c::after{background-color:#1677ff;bottom:0;content:\x22\x22;height:",
      [0, 4],
      ";left:0;position:absolute;width:100%}\n.",
      [1],
      "content-panel.",
      [1],
      "data-v-7f55b84c{padding:",
      [0, 30],
      "}\n.",
      [1],
      "battery1-bg.",
      [1],
      "data-v-7f55b84c{background-color:#f5f7fa}\n.",
      [1],
      "battery2-bg.",
      [1],
      "data-v-7f55b84c{background-color:#e6f7ff}\n.",
      [1],
      "param-form.",
      [1],
      "data-v-7f55b84c{padding:",
      [0, 30],
      "}\n.",
      [1],
      "form-item.",
      [1],
      "data-v-7f55b84c{-webkit-align-items:center;align-items:center;display:-webkit-flex;display:flex;margin-bottom:",
      [0, 30],
      "}\n.",
      [1],
      "param-label.",
      [1],
      "data-v-7f55b84c{color:#333;font-size:",
      [0, 28],
      ";width:",
      [0, 240],
      "}\n.",
      [1],
      "param-input.",
      [1],
      "data-v-7f55b84c{border:1px solid #d9d9d9;border-radius:",
      [0, 6],
      ";color:#333;-webkit-flex:1;flex:1;font-size:",
      [0, 28],
      ";height:",
      [0, 70],
      ";padding:0 ",
      [0, 20],
      "}\n.",
      [1],
      "button-group.",
      [1],
      "data-v-7f55b84c{display:-webkit-flex;display:flex;-webkit-flex-direction:row;flex-direction:row;gap:",
      [0, 20],
      ";margin-top:",
      [0, 40],
      "}\n.",
      [1],
      "btn.",
      [1],
      "data-v-7f55b84c{border-radius:",
      [0, 8],
      ";-webkit-flex:1;flex:1;font-size:",
      [0, 30],
      ";height:",
      [0, 80],
      ";line-height:",
      [0, 80],
      ";padding:0}\n.",
      [1],
      "reset-btn.",
      [1],
      "data-v-7f55b84c{background-color:#f5f5f5;border:none;color:#333}\n.",
      [1],
      "confirm-btn.",
      [1],
      "data-v-7f55b84c{background-color:#1677ff;border:none;color:#fff}\n.",
      [1],
      "cancel-btn.",
      [1],
      "data-v-7f55b84c{background-color:#fff;border:1px solid #d9d9d9;color:#666}\n",
    ],
    undefined,
    { path: "./subpackages/info/battery/battery.wxss" }
  );
}

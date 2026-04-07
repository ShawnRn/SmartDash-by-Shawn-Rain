$gwx2_XC_2 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx2_XC_2 || [];
    function gz$gwx2_XC_2_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx2_XC_2_1)
        return __WXML_GLOBAL__.ops_cached.$gwx2_XC_2_1;
      __WXML_GLOBAL__.ops_cached.$gwx2_XC_2_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([3, "container data-v-4c0e4f61"]);
        Z([3, "data-v-4c0e4f61"]);
        Z([3, "image-container data-v-4c0e4f61"]);
        Z([3, "aspectFit"]);
        Z([
          3,
          "https://tse3-mm.cn.bing.net/th/id/OIP-C.3TVtJcqPrPgMHrw3kRvMGwHaDp?rs\x3d1\x26pid\x3dImgDetMain",
        ]);
        Z([3, "search-title data-v-4c0e4f61"]);
        Z([3, "快捷检索"]);
        Z([3, "__e"]);
        Z([3, "search-input data-v-4c0e4f61"]);
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
                      [5],
                      [
                        [4],
                        [
                          [5],
                          [[5], [1, "__set_model"]],
                          [
                            [4],
                            [
                              [5],
                              [
                                [5],
                                [[5], [[5], [1, ""]], [1, "searchQuery"]],
                                [1, "$event"],
                              ],
                              [[4], [[5]]],
                            ],
                          ],
                        ],
                      ],
                    ],
                    [
                      [4],
                      [
                        [5],
                        [[5], [1, "handleSearchChange"]],
                        [[4], [[5], [1, "$event"]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z([3, "搜索问题..."]);
        Z([3, "text"]);
        Z([[7], [3, "searchQuery"]]);
        Z(z[12]);
        Z([3, "question-list data-v-4c0e4f61"]);
        Z([3, "index"]);
        Z([3, "questionObj"]);
        Z([[7], [3, "filteredQuestions"]]);
        Z(z[15]);
        Z(z[7]);
        Z([3, "question-item data-v-4c0e4f61"]);
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
                        [
                          [5],
                          [[5], [1, "handleQuestionClick"]],
                          [[4], [[5], [1, "$0"]]],
                        ],
                        [
                          [4],
                          [
                            [5],
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
                                      [[5], [1, "filteredQuestions"]],
                                      [1, ""],
                                    ],
                                    [[7], [3, "index"]],
                                  ],
                                ],
                              ],
                            ],
                          ],
                        ],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z([
          a,
          [
            [2, "+"],
            [
              [2, "+"],
              [1, ""],
              [[6], [[7], [3, "questionObj"]], [3, "question"]],
            ],
            [1, ""],
          ],
        ]);
        Z([[7], [3, "selectedQuestion"]]);
        Z([3, "modal data-v-4c0e4f61"]);
        Z([3, "modal-content data-v-4c0e4f61"]);
        Z([3, "modal-title data-v-4c0e4f61"]);
        Z([a, [[6], [[7], [3, "selectedQuestion"]], [3, "question"]]]);
        Z([3, "modal-solution data-v-4c0e4f61"]);
        Z([a, [[6], [[7], [3, "selectedQuestion"]], [3, "solution"]]]);
        Z(z[7]);
        Z([3, "modal-close data-v-4c0e4f61"]);
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
                        [[5], [1, "handleCloseModal"]],
                        [[4], [[5], [1, "$event"]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z([3, "关闭"]);
        Z([3, "row row-1 data-v-4c0e4f61"]);
        Z(z[7]);
        Z([3, "col col-1-1 bg-blue data-v-4c0e4f61"]);
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
                        [[5], [1, "faq_click_handle"]],
                        [[4], [[5], [1, 0]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z([3, "icon-container data-v-4c0e4f61"]);
        Z([3, "cuIcon-similar data-v-4c0e4f61"]);
        Z([3, "text-container data-v-4c0e4f61"]);
        Z(z[1]);
        Z([a, [[6], [[7], [3, "$root"]], [3, "m0"]]]);
        Z([3, "right-cols data-v-4c0e4f61"]);
        Z(z[7]);
        Z([3, "col col-1-2 bg-orange data-v-4c0e4f61"]);
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
                        [[5], [1, "faq_click_handle"]],
                        [[4], [[5], [1, 1]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z(z[38]);
        Z([3, "cuIcon-camerarotate data-v-4c0e4f61"]);
        Z(z[40]);
        Z(z[1]);
        Z([a, [[6], [[7], [3, "$root"]], [3, "m1"]]]);
        Z(z[7]);
        Z([3, "col col-1-3 bg-green data-v-4c0e4f61"]);
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
                        [[5], [1, "faq_click_handle"]],
                        [[4], [[5], [1, 2]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z(z[38]);
        Z([3, "cuIcon-flashlightopen data-v-4c0e4f61"]);
        Z(z[40]);
        Z(z[1]);
        Z([a, [[6], [[7], [3, "$root"]], [3, "m2"]]]);
        Z([3, "row row-2 data-v-4c0e4f61"]);
        Z(z[7]);
        Z([3, "col col-2-1 bg-brown data-v-4c0e4f61"]);
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
                        [[5], [1, "faq_click_handle"]],
                        [[4], [[5], [1, 3]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z(z[38]);
        Z([3, "cuIcon-keyboard data-v-4c0e4f61"]);
        Z(z[40]);
        Z(z[1]);
        Z([a, [[6], [[7], [3, "$root"]], [3, "m3"]]]);
        Z(z[7]);
        Z([3, "col col-2-2 bg-mauve data-v-4c0e4f61"]);
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
                        [[5], [1, "faq_click_handle"]],
                        [[4], [[5], [1, 4]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z(z[38]);
        Z([3, "cuIcon-focus data-v-4c0e4f61"]);
        Z(z[40]);
        Z(z[1]);
        Z([a, [[6], [[7], [3, "$root"]], [3, "m4"]]]);
        Z(z[7]);
        Z([3, "col col-2-3 bg-olive data-v-4c0e4f61"]);
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
                        [[5], [1, "faq_click_handle"]],
                        [[4], [[5], [1, 5]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z(z[38]);
        Z([3, "cuIcon-bad data-v-4c0e4f61"]);
        Z(z[40]);
        Z(z[1]);
        Z([a, [[6], [[7], [3, "$root"]], [3, "m5"]]]);
      })(__WXML_GLOBAL__.ops_cached.$gwx2_XC_2_1);
      return __WXML_GLOBAL__.ops_cached.$gwx2_XC_2_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx2_XC_2 = z;
    __WXML_GLOBAL__.ops_init.$gwx2_XC_2 = true;
    var x = ["./subpackages/info/pages/faq/faq.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx2_XC_2_1();
      var oLD = _n("view");
      _rz(z, oLD, "class", 0, e, s, gg);
      var lOD = _n("view");
      _rz(z, lOD, "class", 1, e, s, gg);
      var aPD = _mz(
        z,
        "image",
        ["class", 2, "mode", 1, "src", 2],
        [],
        e,
        s,
        gg
      );
      _(lOD, aPD);
      _(oLD, lOD);
      var tQD = _n("view");
      _rz(z, tQD, "class", 5, e, s, gg);
      var eRD = _oz(z, 6, e, s, gg);
      _(tQD, eRD);
      _(oLD, tQD);
      var bSD = _mz(
        z,
        "input",
        [
          "bindinput",
          7,
          "class",
          1,
          "data-event-opts",
          2,
          "placeholder",
          3,
          "type",
          4,
          "value",
          5,
        ],
        [],
        e,
        s,
        gg
      );
      _(oLD, bSD);
      var cMD = _v();
      _(oLD, cMD);
      if (_oz(z, 13, e, s, gg)) {
        cMD.wxVkey = 1;
        var oTD = _n("view");
        _rz(z, oTD, "class", 14, e, s, gg);
        var xUD = _v();
        _(oTD, xUD);
        var oVD = function (cXD, fWD, hYD, gg) {
          var c1D = _mz(
            z,
            "view",
            ["bindtap", 19, "class", 1, "data-event-opts", 2],
            [],
            cXD,
            fWD,
            gg
          );
          var o2D = _oz(z, 22, cXD, fWD, gg);
          _(c1D, o2D);
          _(hYD, c1D);
          return hYD;
        };
        xUD.wxXCkey = 2;
        _2z(z, 17, oVD, e, s, gg, xUD, "questionObj", "index", "index");
        _(cMD, oTD);
      }
      var oND = _v();
      _(oLD, oND);
      if (_oz(z, 23, e, s, gg)) {
        oND.wxVkey = 1;
        var l3D = _n("view");
        _rz(z, l3D, "class", 24, e, s, gg);
        var a4D = _n("view");
        _rz(z, a4D, "class", 25, e, s, gg);
        var t5D = _n("view");
        _rz(z, t5D, "class", 26, e, s, gg);
        var e6D = _oz(z, 27, e, s, gg);
        _(t5D, e6D);
        _(a4D, t5D);
        var b7D = _n("view");
        _rz(z, b7D, "class", 28, e, s, gg);
        var o8D = _oz(z, 29, e, s, gg);
        _(b7D, o8D);
        _(a4D, b7D);
        var x9D = _mz(
          z,
          "button",
          ["bindtap", 30, "class", 1, "data-event-opts", 2],
          [],
          e,
          s,
          gg
        );
        var o0D = _oz(z, 33, e, s, gg);
        _(x9D, o0D);
        _(a4D, x9D);
        _(l3D, a4D);
        _(oND, l3D);
      }
      var fAE = _n("view");
      _rz(z, fAE, "class", 34, e, s, gg);
      var cBE = _mz(
        z,
        "view",
        ["bindtap", 35, "class", 1, "data-event-opts", 2],
        [],
        e,
        s,
        gg
      );
      var hCE = _n("view");
      _rz(z, hCE, "class", 38, e, s, gg);
      var oDE = _n("text");
      _rz(z, oDE, "class", 39, e, s, gg);
      _(hCE, oDE);
      _(cBE, hCE);
      var cEE = _n("view");
      _rz(z, cEE, "class", 40, e, s, gg);
      var oFE = _n("text");
      _rz(z, oFE, "class", 41, e, s, gg);
      var lGE = _oz(z, 42, e, s, gg);
      _(oFE, lGE);
      _(cEE, oFE);
      _(cBE, cEE);
      _(fAE, cBE);
      var aHE = _n("view");
      _rz(z, aHE, "class", 43, e, s, gg);
      var tIE = _mz(
        z,
        "view",
        ["bindtap", 44, "class", 1, "data-event-opts", 2],
        [],
        e,
        s,
        gg
      );
      var eJE = _n("view");
      _rz(z, eJE, "class", 47, e, s, gg);
      var bKE = _n("text");
      _rz(z, bKE, "class", 48, e, s, gg);
      _(eJE, bKE);
      _(tIE, eJE);
      var oLE = _n("view");
      _rz(z, oLE, "class", 49, e, s, gg);
      var xME = _n("text");
      _rz(z, xME, "class", 50, e, s, gg);
      var oNE = _oz(z, 51, e, s, gg);
      _(xME, oNE);
      _(oLE, xME);
      _(tIE, oLE);
      _(aHE, tIE);
      var fOE = _mz(
        z,
        "view",
        ["bindtap", 52, "class", 1, "data-event-opts", 2],
        [],
        e,
        s,
        gg
      );
      var cPE = _n("view");
      _rz(z, cPE, "class", 55, e, s, gg);
      var hQE = _n("text");
      _rz(z, hQE, "class", 56, e, s, gg);
      _(cPE, hQE);
      _(fOE, cPE);
      var oRE = _n("view");
      _rz(z, oRE, "class", 57, e, s, gg);
      var cSE = _n("text");
      _rz(z, cSE, "class", 58, e, s, gg);
      var oTE = _oz(z, 59, e, s, gg);
      _(cSE, oTE);
      _(oRE, cSE);
      _(fOE, oRE);
      _(aHE, fOE);
      _(fAE, aHE);
      _(oLD, fAE);
      var lUE = _n("view");
      _rz(z, lUE, "class", 60, e, s, gg);
      var aVE = _mz(
        z,
        "view",
        ["bindtap", 61, "class", 1, "data-event-opts", 2],
        [],
        e,
        s,
        gg
      );
      var tWE = _n("view");
      _rz(z, tWE, "class", 64, e, s, gg);
      var eXE = _n("text");
      _rz(z, eXE, "class", 65, e, s, gg);
      _(tWE, eXE);
      _(aVE, tWE);
      var bYE = _n("view");
      _rz(z, bYE, "class", 66, e, s, gg);
      var oZE = _n("text");
      _rz(z, oZE, "class", 67, e, s, gg);
      var x1E = _oz(z, 68, e, s, gg);
      _(oZE, x1E);
      _(bYE, oZE);
      _(aVE, bYE);
      _(lUE, aVE);
      var o2E = _mz(
        z,
        "view",
        ["bindtap", 69, "class", 1, "data-event-opts", 2],
        [],
        e,
        s,
        gg
      );
      var f3E = _n("view");
      _rz(z, f3E, "class", 72, e, s, gg);
      var c4E = _n("text");
      _rz(z, c4E, "class", 73, e, s, gg);
      _(f3E, c4E);
      _(o2E, f3E);
      var h5E = _n("view");
      _rz(z, h5E, "class", 74, e, s, gg);
      var o6E = _n("text");
      _rz(z, o6E, "class", 75, e, s, gg);
      var c7E = _oz(z, 76, e, s, gg);
      _(o6E, c7E);
      _(h5E, o6E);
      _(o2E, h5E);
      _(lUE, o2E);
      var o8E = _mz(
        z,
        "view",
        ["bindtap", 77, "class", 1, "data-event-opts", 2],
        [],
        e,
        s,
        gg
      );
      var l9E = _n("view");
      _rz(z, l9E, "class", 80, e, s, gg);
      var a0E = _n("text");
      _rz(z, a0E, "class", 81, e, s, gg);
      _(l9E, a0E);
      _(o8E, l9E);
      var tAF = _n("view");
      _rz(z, tAF, "class", 82, e, s, gg);
      var eBF = _n("text");
      _rz(z, eBF, "class", 83, e, s, gg);
      var bCF = _oz(z, 84, e, s, gg);
      _(eBF, bCF);
      _(tAF, eBF);
      _(o8E, tAF);
      _(lUE, o8E);
      _(oLD, lUE);
      cMD.wxXCkey = 1;
      oND.wxXCkey = 1;
      _(r, oLD);
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      outerGlobal.__wxml_comp_version__ = 0.02;
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx2_XC_2";
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
if (__vd_version_info__.delayedGwx || false) $gwx2_XC_2();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/info/pages/faq/faq.wxml"] = [
    $gwx2_XC_2,
    "./subpackages/info/pages/faq/faq.wxml",
  ];
else
  __wxAppCode__["subpackages/info/pages/faq/faq.wxml"] = $gwx2_XC_2(
    "./subpackages/info/pages/faq/faq.wxml"
  );

var noCss =
  typeof __vd_version_info__ !== "undefined" &&
  __vd_version_info__.noCss === true;
if (!noCss) {
  __wxAppCode__["subpackages/info/pages/faq/faq.wxss"] = setCssToHead(
    [
      ".",
      [1],
      "container.",
      [1],
      "data-v-4c0e4f61{background-color:#e6edff;height:100vh;padding:10px}\n.",
      [1],
      "image-container.",
      [1],
      "data-v-4c0e4f61{height:",
      [0, 400],
      ";width:100%}\n.",
      [1],
      "row.",
      [1],
      "data-v-4c0e4f61{display:-webkit-flex;display:flex;margin-bottom:10px}\n.",
      [1],
      "row-1 .",
      [1],
      "col-1-1.",
      [1],
      "data-v-4c0e4f61{margin-right:10px;margin-top:",
      [0, 40],
      ";width:33.33%}\n.",
      [1],
      "row-1 .",
      [1],
      "right-cols.",
      [1],
      "data-v-4c0e4f61{display:-webkit-flex;display:flex;-webkit-flex-direction:column;flex-direction:column;margin-top:",
      [0, 40],
      ";width:66.67%}\n.",
      [1],
      "row-1 .",
      [1],
      "right-cols .",
      [1],
      "col.",
      [1],
      "data-v-4c0e4f61{height:50%}\n.",
      [1],
      "row-1 .",
      [1],
      "right-cols .",
      [1],
      "col-1-2.",
      [1],
      "data-v-4c0e4f61{margin-bottom:10px}\n.",
      [1],
      "row-2 .",
      [1],
      "col-2-1.",
      [1],
      "data-v-4c0e4f61{margin-right:10px;width:50%}\n.",
      [1],
      "row-2 .",
      [1],
      "col-2-2.",
      [1],
      "data-v-4c0e4f61,.",
      [1],
      "row-2 .",
      [1],
      "col-2-3.",
      [1],
      "data-v-4c0e4f61{width:25%}\n.",
      [1],
      "row-2 .",
      [1],
      "col-2-2.",
      [1],
      "data-v-4c0e4f61{margin-right:10px}\n.",
      [1],
      "col.",
      [1],
      "data-v-4c0e4f61{border:1px solid #ccc;box-shadow:0 2px 4px rgba(0,0,0,.1);padding:10px;text-align:center}\n.",
      [1],
      "col.",
      [1],
      "data-v-4c0e4f61:active{box-shadow:0 0 10px rgba(0,0,0,.3);-webkit-transform:scale(.9);transform:scale(.9)}\n.",
      [1],
      "icon-container.",
      [1],
      "data-v-4c0e4f61{color:#fff;font-size:36px;margin-bottom:10px}\n.",
      [1],
      "text-container.",
      [1],
      "data-v-4c0e4f61{color:#fff;font-size:16px}\n.",
      [1],
      "search-title.",
      [1],
      "data-v-4c0e4f61{font-size:18px;font-weight:700;margin-bottom:10px;margin-top:5px}\n.",
      [1],
      "search-input.",
      [1],
      "data-v-4c0e4f61{border:1px solid #ccc;border-radius:5px;height:auto;margin-bottom:20px;padding:10px;width:100%}\n.",
      [1],
      "question-list.",
      [1],
      "data-v-4c0e4f61{list-style:none;padding:0}\n.",
      [1],
      "question-item.",
      [1],
      "data-v-4c0e4f61{border-bottom:1px solid #eee;cursor:pointer;padding:10px}\n.",
      [1],
      "question-item.",
      [1],
      "data-v-4c0e4f61:hover{background-color:#f5f5f5}\n.",
      [1],
      "modal.",
      [1],
      "data-v-4c0e4f61{-webkit-align-items:center;align-items:center;background-color:rgba(0,0,0,.5);bottom:0;display:-webkit-flex;display:flex;-webkit-justify-content:center;justify-content:center;left:0;position:fixed;right:0;top:0}\n.",
      [1],
      "modal-content.",
      [1],
      "data-v-4c0e4f61{background-color:#fff;border-radius:5px;padding:20px;width:80%}\n.",
      [1],
      "modal-title.",
      [1],
      "data-v-4c0e4f61{font-size:18px;font-weight:700;margin-bottom:10px}\n.",
      [1],
      "modal-solution.",
      [1],
      "data-v-4c0e4f61{margin-bottom:20px}\n.",
      [1],
      "modal-close.",
      [1],
      "data-v-4c0e4f61{background-color:#007aff;border:none;border-radius:5px;color:#fff;cursor:pointer;padding:10px 20px}\n",
    ],
    undefined,
    { path: "./subpackages/info/pages/faq/faq.wxss" }
  );
}

$gwx_XC_3 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx_XC_3 || [];
    function gz$gwx_XC_3_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx_XC_3_1)
        return __WXML_GLOBAL__.ops_cached.$gwx_XC_3_1;
      __WXML_GLOBAL__.ops_cached.$gwx_XC_3_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([3, "page"]);
        Z([1, true]);
        Z([3, "response"]);
        Z([3, "widthFix"]);
        Z([
          3,
          "https://cdn.nlark.com/yuque/0/2019/png/280374/1552996358228-assets/web-upload/e256b4ce-d9a4-488b-8da2-032747213982.png",
        ]);
        Z([3, "cu-tabbar-height"]);
        Z([3, "nav-list"]);
        Z([3, "index"]);
        Z([3, "item"]);
        Z([[7], [3, "elements"]]);
        Z(z[7]);
        Z([3, "__e"]);
        Z([
          [4],
          [
            [5],
            [[5], [1, "nav-li"]],
            [
              [2, "+"],
              [1, "bg-"],
              [[6], [[7], [3, "item"]], [3, "color"]],
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
                    [
                      [4],
                      [
                        [5],
                        [[5], [1, "change_language_event"]],
                        [[4], [[5], [[7], [3, "index"]]]],
                      ],
                    ],
                  ],
                ],
              ],
            ],
          ],
        ]);
        Z(z[1]);
        Z([
          [2, "+"],
          [
            [2, "+"],
            [1, "animation:"],
            [
              [2, "+"],
              [
                [2, "+"],
                [1, "show "],
                [
                  [2, "+"],
                  [
                    [2, "*"],
                    [
                      [2, "+"],
                      [[7], [3, "index"]],
                      [1, 1],
                    ],
                    [1, 0.2],
                  ],
                  [1, 1],
                ],
              ],
              [1, "s 1"],
            ],
          ],
          [1, ";"],
        ]);
        Z([3, "nav-title"]);
        Z([a, [[6], [[7], [3, "item"]], [3, "title"]]]);
        Z([3, "nav-name"]);
        Z([a, [[6], [[7], [3, "item"]], [3, "name"]]]);
        Z([
          [4],
          [
            [5],
            [
              [2, "+"],
              [1, "cuIcon-"],
              [[6], [[7], [3, "item"]], [3, "cuIcon"]],
            ],
          ],
        ]);
        Z([3, "content-display"]);
        Z([3, "aspectFill"]);
        Z([
          3,
          "https://img1.baidu.com/it/u\x3d1810354550,4111141332\x26fm\x3d253\x26fmt\x3dauto\x26app\x3d138\x26f\x3dJPEG?w\x3d833\x26h\x3d500",
        ]);
        Z([3, "description bg-gradual-blue"]);
        Z([3, "text-white"]);
        Z([a, [[6], [[7], [3, "$root"]], [3, "m0"]]]);
      })(__WXML_GLOBAL__.ops_cached.$gwx_XC_3_1);
      return __WXML_GLOBAL__.ops_cached.$gwx_XC_3_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx_XC_3 = z;
    __WXML_GLOBAL__.ops_init.$gwx_XC_3 = true;
    var x = ["./pages/language/language.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx_XC_3_1();
      var bAO = _n("view");
      var oBO = _mz(z, "scroll-view", ["class", 0, "scrollY", 1], [], e, s, gg);
      var xCO = _mz(
        z,
        "image",
        ["class", 2, "mode", 1, "src", 2],
        [],
        e,
        s,
        gg
      );
      _(oBO, xCO);
      var oDO = _n("view");
      _rz(z, oDO, "class", 5, e, s, gg);
      _(oBO, oDO);
      var fEO = _n("view");
      _rz(z, fEO, "class", 6, e, s, gg);
      var cFO = _v();
      _(fEO, cFO);
      var hGO = function (cIO, oHO, oJO, gg) {
        var aLO = _mz(
          z,
          "view",
          [
            "bindtap",
            11,
            "class",
            1,
            "data-event-opts",
            2,
            "navigateTo",
            3,
            "style",
            4,
          ],
          [],
          cIO,
          oHO,
          gg
        );
        var tMO = _n("view");
        _rz(z, tMO, "class", 16, cIO, oHO, gg);
        var eNO = _oz(z, 17, cIO, oHO, gg);
        _(tMO, eNO);
        _(aLO, tMO);
        var bOO = _n("view");
        _rz(z, bOO, "class", 18, cIO, oHO, gg);
        var oPO = _oz(z, 19, cIO, oHO, gg);
        _(bOO, oPO);
        _(aLO, bOO);
        var xQO = _n("text");
        _rz(z, xQO, "class", 20, cIO, oHO, gg);
        _(aLO, xQO);
        _(oJO, aLO);
        return oJO;
      };
      cFO.wxXCkey = 2;
      _2z(z, 9, hGO, e, s, gg, cFO, "item", "index", "index");
      _(oBO, fEO);
      var oRO = _n("view");
      _rz(z, oRO, "class", 21, e, s, gg);
      var fSO = _mz(z, "image", ["mode", 22, "src", 1], [], e, s, gg);
      _(oRO, fSO);
      var cTO = _n("view");
      _rz(z, cTO, "class", 24, e, s, gg);
      var hUO = _n("text");
      _rz(z, hUO, "class", 25, e, s, gg);
      var oVO = _oz(z, 26, e, s, gg);
      _(hUO, oVO);
      _(cTO, hUO);
      _(oRO, cTO);
      _(oBO, oRO);
      _(bAO, oBO);
      _(r, bAO);
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      outerGlobal.__wxml_comp_version__ = 0.02;
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx_XC_3";
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
if (__vd_version_info__.delayedGwx || false) $gwx_XC_3();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["pages/language/language.wxml"] = [
    $gwx_XC_3,
    "./pages/language/language.wxml",
  ];
else
  __wxAppCode__["pages/language/language.wxml"] = $gwx_XC_3(
    "./pages/language/language.wxml"
  );

var noCss =
  typeof __vd_version_info__ !== "undefined" &&
  __vd_version_info__.noCss === true;
if (!noCss) {
  __wxAppCode__["pages/language/language.wxss"] = setCssToHead(
    [
      ".",
      [1],
      "page{height:100vh}\n.",
      [1],
      "nav-list{display:-webkit-flex;display:flex;-webkit-flex-wrap:wrap;flex-wrap:wrap;-webkit-justify-content:space-between;justify-content:space-between;padding:0 ",
      [0, 40],
      "}\n.",
      [1],
      "nav-li{background-image:url(https://cdn.nlark.com/yuque/0/2019/png/280374/1552996358352-assets/web-upload/cc3b1807-c684-4b83-8f80-80e5b8a6b975.png);background-position:50%;background-size:cover;border-radius:",
      [0, 12],
      ";margin:0 2.5% ",
      [0, 40],
      ";padding:",
      [0, 30],
      ";position:relative;width:45%;z-index:1}\n.",
      [1],
      "nav-li::after{background-color:inherit;border-radius:",
      [0, 10],
      ";bottom:-10%;content:\x22\x22;height:100%;left:0;opacity:.2;position:absolute;-webkit-transform:scale(.9,.9);transform:scale(.9,.9);width:100%;z-index:-1}\n.",
      [1],
      "nav-li.",
      [1],
      "cur{background:#5eb95e;box-shadow:",
      [0, 4],
      " ",
      [0, 4],
      " ",
      [0, 6],
      " rgba(94,185,94,.4);color:#fff}\n.",
      [1],
      "nav-title{font-size:",
      [0, 32],
      ";font-weight:300}\n.",
      [1],
      "nav-title::first-letter{font-size:",
      [0, 40],
      ";margin-right:",
      [0, 4],
      "}\n.",
      [1],
      "nav-name{font-size:",
      [0, 28],
      ";margin-top:",
      [0, 20],
      ";position:relative;text-transform:Capitalize}\n.",
      [1],
      "nav-name::before{height:",
      [0, 6],
      ";opacity:.5;right:0;width:",
      [0, 40],
      "}\n.",
      [1],
      "nav-name::after,.",
      [1],
      "nav-name::before{background:#fff;bottom:0;content:\x22\x22;display:block;position:absolute}\n.",
      [1],
      "nav-name::after{height:1px;opacity:.3;right:",
      [0, 40],
      ";width:",
      [0, 100],
      "}\n.",
      [1],
      "nav-name::first-letter{font-size:",
      [0, 36],
      ";font-weight:700;margin-right:1px}\n.",
      [1],
      "nav-li wx-text{font-size:",
      [0, 52],
      ";height:",
      [0, 60],
      ";line-height:",
      [0, 60],
      ";position:absolute;right:",
      [0, 30],
      ";text-align:center;top:",
      [0, 30],
      ";width:",
      [0, 60],
      "}\n.",
      [1],
      "text-light{font-weight:300}\n@keyframes show{0%{-webkit-transform:translateY(-50px);transform:translateY(-50px)}\n60%{-webkit-transform:translateY(",
      [0, 40],
      ");transform:translateY(",
      [0, 40],
      ")}\n100%{-webkit-transform:translateY(0);transform:translateY(0)}\n}@-webkit-keyframes show{0%{-webkit-transform:translateY(-50px);transform:translateY(-50px)}\n60%{-webkit-transform:translateY(",
      [0, 40],
      ");transform:translateY(",
      [0, 40],
      ")}\n100%{-webkit-transform:translateY(0);transform:translateY(0)}\n}.",
      [1],
      "content-display{margin-top:20px;text-align:center}\n.",
      [1],
      "content-display wx-image{border-radius:50%;height:200px;overflow:hidden;width:200px}\n.",
      [1],
      "description{color:#333;font-size:16px}\n",
    ],
    "Some selectors are not allowed in component wxss, including tag name selectors, ID selectors, and attribute selectors.(./pages/language/language.wxss:1:1917)",
    { path: "./pages/language/language.wxss" }
  );
}

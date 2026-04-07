$gwx2_XC_5 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx2_XC_5 || [];
    function gz$gwx2_XC_5_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx2_XC_5_1)
        return __WXML_GLOBAL__.ops_cached.$gwx2_XC_5_1;
      __WXML_GLOBAL__.ops_cached.$gwx2_XC_5_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([3, "update-record-container data-v-4d133136"]);
        Z([3, "top-image data-v-4d133136"]);
        Z([3, "widthFix"]);
        Z([
          3,
          "https://imgo.liulanqi.net/img2021/5/18/10/2021051803743616.jpg",
        ]);
        Z([3, "title data-v-4d133136"]);
        Z([3, "小程序版本更新记录"]);
        Z([3, "index"]);
        Z([3, "record"]);
        Z([[7], [3, "updateRecords"]]);
        Z(z[6]);
        Z([3, "record-item data-v-4d133136"]);
        Z([3, "version data-v-4d133136"]);
        Z([a, [[6], [[7], [3, "record"]], [3, "version"]]]);
        Z([3, "content data-v-4d133136"]);
        Z([3, "i"]);
        Z([3, "item"]);
        Z([[6], [[7], [3, "record"]], [3, "content"]]);
        Z(z[14]);
        Z([3, "content-item data-v-4d133136"]);
        Z([
          a,
          [
            [2, "+"],
            [
              [2, "+"],
              [1, "- "],
              [[7], [3, "item"]],
            ],
            [1, ""],
          ],
        ]);
      })(__WXML_GLOBAL__.ops_cached.$gwx2_XC_5_1);
      return __WXML_GLOBAL__.ops_cached.$gwx2_XC_5_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx2_XC_5 = z;
    __WXML_GLOBAL__.ops_init.$gwx2_XC_5 = true;
    var x = ["./subpackages/info/pages/version/version.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx2_XC_5_1();
      var c5G = _n("view");
      _rz(z, c5G, "class", 0, e, s, gg);
      var o6G = _mz(
        z,
        "image",
        ["class", 1, "mode", 1, "src", 2],
        [],
        e,
        s,
        gg
      );
      _(c5G, o6G);
      var l7G = _n("view");
      _rz(z, l7G, "class", 4, e, s, gg);
      var a8G = _oz(z, 5, e, s, gg);
      _(l7G, a8G);
      _(c5G, l7G);
      var t9G = _v();
      _(c5G, t9G);
      var e0G = function (oBH, bAH, xCH, gg) {
        var fEH = _n("view");
        _rz(z, fEH, "class", 10, oBH, bAH, gg);
        var cFH = _n("view");
        _rz(z, cFH, "class", 11, oBH, bAH, gg);
        var hGH = _oz(z, 12, oBH, bAH, gg);
        _(cFH, hGH);
        _(fEH, cFH);
        var oHH = _n("view");
        _rz(z, oHH, "class", 13, oBH, bAH, gg);
        var cIH = _v();
        _(oHH, cIH);
        var oJH = function (aLH, lKH, tMH, gg) {
          var bOH = _n("view");
          _rz(z, bOH, "class", 18, aLH, lKH, gg);
          var oPH = _oz(z, 19, aLH, lKH, gg);
          _(bOH, oPH);
          _(tMH, bOH);
          return tMH;
        };
        cIH.wxXCkey = 2;
        _2z(z, 16, oJH, oBH, bAH, gg, cIH, "item", "i", "i");
        _(fEH, oHH);
        _(xCH, fEH);
        return xCH;
      };
      t9G.wxXCkey = 2;
      _2z(z, 8, e0G, e, s, gg, t9G, "record", "index", "index");
      _(r, c5G);
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      outerGlobal.__wxml_comp_version__ = 0.02;
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx2_XC_5";
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
if (__vd_version_info__.delayedGwx || false) $gwx2_XC_5();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/info/pages/version/version.wxml"] = [
    $gwx2_XC_5,
    "./subpackages/info/pages/version/version.wxml",
  ];
else
  __wxAppCode__["subpackages/info/pages/version/version.wxml"] = $gwx2_XC_5(
    "./subpackages/info/pages/version/version.wxml"
  );

var noCss =
  typeof __vd_version_info__ !== "undefined" &&
  __vd_version_info__.noCss === true;
if (!noCss) {
  __wxAppCode__["subpackages/info/pages/version/version.wxss"] = setCssToHead(
    [
      ".",
      [1],
      "update-record-container.",
      [1],
      "data-v-4d133136{background-color:#e6edff;padding:20px}\n.",
      [1],
      "top-image.",
      [1],
      "data-v-4d133136{margin-bottom:20px;width:100%}\n.",
      [1],
      "title.",
      [1],
      "data-v-4d133136{font-size:24px;font-weight:700;margin-bottom:20px;text-align:center}\n.",
      [1],
      "record-item.",
      [1],
      "data-v-4d133136{border:1px solid #ccc;border-radius:8px;box-shadow:0 2px 4px rgba(0,0,0,.1);margin-bottom:20px;padding:15px}\n.",
      [1],
      "version.",
      [1],
      "data-v-4d133136{font-size:18px;font-weight:700;margin-bottom:10px}\n.",
      [1],
      "content.",
      [1],
      "data-v-4d133136{color:#666;font-size:14px}\n.",
      [1],
      "content-item.",
      [1],
      "data-v-4d133136{margin-bottom:5px}\n",
    ],
    undefined,
    { path: "./subpackages/info/pages/version/version.wxss" }
  );
}

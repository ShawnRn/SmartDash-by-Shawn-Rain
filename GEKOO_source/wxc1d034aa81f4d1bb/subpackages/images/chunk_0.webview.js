$gwx3_XC_0 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx3_XC_0 || [];
    function gz$gwx3_XC_0_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx3_XC_0_1)
        return __WXML_GLOBAL__.ops_cached.$gwx3_XC_0_1;
      __WXML_GLOBAL__.ops_cached.$gwx3_XC_0_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([[7], [3, "showTransition"]]);
        Z([
          [4],
          [
            [5],
            [[5], [[5], [1, "card"]], [1, "data-v-44146ce8"]],
            [
              [2, "?:"],
              [[7], [3, "transitionCompleted"]],
              [1, "active"],
              [1, ""],
            ],
          ],
        ]);
        Z([
          [2, "+"],
          [
            [2, "+"],
            [1, "background:"],
            [[7], [3, "color"]],
          ],
          [1, ";"],
        ]);
        Z([3, "logo-container data-v-44146ce8"]);
        Z([3, "logo-image data-v-44146ce8"]);
        Z([3, "aspectFit"]);
        Z([3, "/static/logo.png"]);
        Z([3, "welcome-text data-v-44146ce8"]);
        Z([a, [[6], [[7], [3, "$root"]], [3, "m0"]]]);
        Z(z[7]);
        Z([a, [[6], [[7], [3, "$root"]], [3, "m1"]]]);
      })(__WXML_GLOBAL__.ops_cached.$gwx3_XC_0_1);
      return __WXML_GLOBAL__.ops_cached.$gwx3_XC_0_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx3_XC_0 = z;
    __WXML_GLOBAL__.ops_init.$gwx3_XC_0 = true;
    var x = ["./subpackages/images/logo/logo.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx3_XC_0_1();
      var oB = _v();
      _(r, oB);
      if (_oz(z, 0, e, s, gg)) {
        oB.wxVkey = 1;
        var xC = _mz(z, "view", ["class", 1, "style", 1], [], e, s, gg);
        var oD = _n("view");
        _rz(z, oD, "class", 3, e, s, gg);
        var fE = _mz(
          z,
          "image",
          ["class", 4, "mode", 1, "src", 2],
          [],
          e,
          s,
          gg
        );
        _(oD, fE);
        var cF = _n("text");
        _rz(z, cF, "class", 7, e, s, gg);
        var hG = _oz(z, 8, e, s, gg);
        _(cF, hG);
        _(oD, cF);
        var oH = _n("text");
        _rz(z, oH, "class", 9, e, s, gg);
        var cI = _oz(z, 10, e, s, gg);
        _(oH, cI);
        _(oD, oH);
        _(xC, oD);
        _(oB, xC);
      }
      oB.wxXCkey = 1;
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      outerGlobal.__wxml_comp_version__ = 0.02;
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx3_XC_0";
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
if (__vd_version_info__.delayedGwx || false) $gwx3_XC_0();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/images/logo/logo.wxml"] = [
    $gwx3_XC_0,
    "./subpackages/images/logo/logo.wxml",
  ];
else
  __wxAppCode__["subpackages/images/logo/logo.wxml"] = $gwx3_XC_0(
    "./subpackages/images/logo/logo.wxml"
  );

var noCss =
  typeof __vd_version_info__ !== "undefined" &&
  __vd_version_info__.noCss === true;
if (!noCss) {
  __wxAppCode__["subpackages/images/logo/logo.wxss"] = setCssToHead(
    [
      ".",
      [1],
      "card.",
      [1],
      "data-v-44146ce8{background:#48d1cc;border-radius:15px;cursor:pointer;height:100vh;overflow:hidden;position:relative;width:100%}\n.",
      [1],
      "card.",
      [1],
      "data-v-44146ce8,.",
      [1],
      "card.",
      [1],
      "data-v-44146ce8::after,.",
      [1],
      "card.",
      [1],
      "data-v-44146ce8::before{-webkit-align-items:center;align-items:center;display:-webkit-flex;display:flex;font-size:25px;font-weight:700;-webkit-justify-content:center;justify-content:center}\n.",
      [1],
      "card.",
      [1],
      "data-v-44146ce8::after,.",
      [1],
      "card.",
      [1],
      "data-v-44146ce8::before{-webkit-animation:expand-data-v-44146ce8 2s cubic-bezier(.215,.61,.355,1) infinite alternate;animation:expand-data-v-44146ce8 2s cubic-bezier(.215,.61,.355,1) infinite alternate;background-color:#fff;content:\x22\x22;height:20%;position:absolute;transition:all 2s;width:20%;z-index:1}\n.",
      [1],
      "card.",
      [1],
      "data-v-44146ce8::before{border-radius:0 15px 0 100%;right:0;top:0;-webkit-transform-origin:top right;transform-origin:top right}\n.",
      [1],
      "card.",
      [1],
      "data-v-44146ce8::after{border-radius:0 100% 0 15px;bottom:0;left:0;-webkit-transform-origin:bottom left;transform-origin:bottom left}\n@-webkit-keyframes expand-data-v-44146ce8{0%{border-radius:15px;height:10%;opacity:.3;-webkit-transform:scale(.1);transform:scale(.1);width:10%}\n100%{border-radius:15px;height:100%;opacity:.8;-webkit-transform:scale(1);transform:scale(1);width:100%}\n}@keyframes expand-data-v-44146ce8{0%{border-radius:15px;height:10%;opacity:.3;-webkit-transform:scale(.1);transform:scale(.1);width:10%}\n100%{border-radius:15px;height:100%;opacity:.8;-webkit-transform:scale(1);transform:scale(1);width:100%}\n}.",
      [1],
      "logo-container.",
      [1],
      "data-v-44146ce8{-webkit-align-items:center;align-items:center;-webkit-animation:fadeInUp-data-v-44146ce8 1.5s ease-out forwards;animation:fadeInUp-data-v-44146ce8 1.5s ease-out forwards;display:-webkit-flex;display:flex;-webkit-flex-direction:column;flex-direction:column;left:50%;position:absolute;top:10%;-webkit-transform:translateX(-50%);transform:translateX(-50%);z-index:2}\n@-webkit-keyframes fadeInUp-data-v-44146ce8{0%{opacity:0;-webkit-transform:translate(-50%,-30%);transform:translate(-50%,-30%)}\n100%{opacity:1;-webkit-transform:translateX(-50%);transform:translateX(-50%)}\n}@keyframes fadeInUp-data-v-44146ce8{0%{opacity:0;-webkit-transform:translate(-50%,-30%);transform:translate(-50%,-30%)}\n100%{opacity:1;-webkit-transform:translateX(-50%);transform:translateX(-50%)}\n}.",
      [1],
      "logo-image.",
      [1],
      "data-v-44146ce8{-webkit-animation:scaleIn-data-v-44146ce8 1.2s ease-out .3s forwards;animation:scaleIn-data-v-44146ce8 1.2s ease-out .3s forwards;max-height:80%;max-width:80%;-webkit-transform-origin:center;transform-origin:center}\n@-webkit-keyframes scaleIn-data-v-44146ce8{0%{-webkit-transform:scale(0);transform:scale(0)}\n100%{-webkit-transform:scale(1);transform:scale(1)}\n}@keyframes scaleIn-data-v-44146ce8{0%{-webkit-transform:scale(0);transform:scale(0)}\n100%{-webkit-transform:scale(1);transform:scale(1)}\n}.",
      [1],
      "welcome-text.",
      [1],
      "data-v-44146ce8{-webkit-animation:fadeIn-data-v-44146ce8 1.2s ease-out .6s forwards;animation:fadeIn-data-v-44146ce8 1.2s ease-out .6s forwards;color:#333;font-size:24px;margin-top:10px}\n@-webkit-keyframes fadeIn-data-v-44146ce8{0%{opacity:0}\n100%{opacity:1}\n}@keyframes fadeIn-data-v-44146ce8{0%{opacity:0}\n100%{opacity:1}\n}",
    ],
    undefined,
    { path: "./subpackages/images/logo/logo.wxss" }
  );
}

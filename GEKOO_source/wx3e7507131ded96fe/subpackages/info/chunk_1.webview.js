$gwx2_XC_1 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx2_XC_1 || [];
    function gz$gwx2_XC_1_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx2_XC_1_1)
        return __WXML_GLOBAL__.ops_cached.$gwx2_XC_1_1;
      __WXML_GLOBAL__.ops_cached.$gwx2_XC_1_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([3, "container data-v-118080e0"]);
        Z([3, "flowchart-title data-v-118080e0"]);
        Z([3, "智科调参步骤说明"]);
        Z([3, "flowchart-steps data-v-118080e0"]);
        Z([3, "index"]);
        Z([3, "step"]);
        Z([[7], [3, "flowchartSteps"]]);
        Z(z[4]);
        Z([3, "step data-v-118080e0"]);
        Z([3, "step-number data-v-118080e0"]);
        Z([
          a,
          [
            [2, "+"],
            [[7], [3, "index"]],
            [1, 1],
          ],
        ]);
        Z([3, "step-content data-v-118080e0"]);
        Z([a, [[7], [3, "step"]]]);
        Z([
          [2, "<"],
          [[7], [3, "index"]],
          [
            [2, "-"],
            [[6], [[7], [3, "$root"]], [3, "g0"]],
            [1, 1],
          ],
        ]);
        Z([3, "step-arrow data-v-118080e0"]);
        Z([3, "→"]);
        Z([3, "warm-tip data-v-118080e0"]);
        Z([3, "tip-title data-v-118080e0"]);
        Z([3, "温馨提示："]);
        Z([3, "tip-content data-v-118080e0"]);
        Z([
          3,
          "参数没有绝对的最优，根据自己的骑行体验调整即可。参数数据有极限，但是骑行的快乐没有极限。",
        ]);
      })(__WXML_GLOBAL__.ops_cached.$gwx2_XC_1_1);
      return __WXML_GLOBAL__.ops_cached.$gwx2_XC_1_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx2_XC_1 = z;
    __WXML_GLOBAL__.ops_init.$gwx2_XC_1 = true;
    var x = ["./subpackages/info/flowchart/flowchart.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx2_XC_1_1();
      var aXC = _n("view");
      _rz(z, aXC, "class", 0, e, s, gg);
      var tYC = _n("view");
      _rz(z, tYC, "class", 1, e, s, gg);
      var eZC = _oz(z, 2, e, s, gg);
      _(tYC, eZC);
      _(aXC, tYC);
      var b1C = _n("view");
      _rz(z, b1C, "class", 3, e, s, gg);
      var o2C = _v();
      _(b1C, o2C);
      var x3C = function (f5C, o4C, c6C, gg) {
        var o8C = _n("view");
        _rz(z, o8C, "class", 8, f5C, o4C, gg);
        var o0C = _n("view");
        _rz(z, o0C, "class", 9, f5C, o4C, gg);
        var lAD = _oz(z, 10, f5C, o4C, gg);
        _(o0C, lAD);
        _(o8C, o0C);
        var aBD = _n("view");
        _rz(z, aBD, "class", 11, f5C, o4C, gg);
        var tCD = _oz(z, 12, f5C, o4C, gg);
        _(aBD, tCD);
        _(o8C, aBD);
        var c9C = _v();
        _(o8C, c9C);
        if (_oz(z, 13, f5C, o4C, gg)) {
          c9C.wxVkey = 1;
          var eDD = _n("view");
          _rz(z, eDD, "class", 14, f5C, o4C, gg);
          var bED = _oz(z, 15, f5C, o4C, gg);
          _(eDD, bED);
          _(c9C, eDD);
        }
        c9C.wxXCkey = 1;
        _(c6C, o8C);
        return c6C;
      };
      o2C.wxXCkey = 2;
      _2z(z, 6, x3C, e, s, gg, o2C, "step", "index", "index");
      _(aXC, b1C);
      var oFD = _n("view");
      _rz(z, oFD, "class", 16, e, s, gg);
      var xGD = _n("text");
      _rz(z, xGD, "class", 17, e, s, gg);
      var oHD = _oz(z, 18, e, s, gg);
      _(xGD, oHD);
      _(oFD, xGD);
      var fID = _n("text");
      _rz(z, fID, "class", 19, e, s, gg);
      var cJD = _oz(z, 20, e, s, gg);
      _(fID, cJD);
      _(oFD, fID);
      _(aXC, oFD);
      _(r, aXC);
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      outerGlobal.__wxml_comp_version__ = 0.02;
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx2_XC_1";
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
if (__vd_version_info__.delayedGwx || false) $gwx2_XC_1();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/info/flowchart/flowchart.wxml"] = [
    $gwx2_XC_1,
    "./subpackages/info/flowchart/flowchart.wxml",
  ];
else
  __wxAppCode__["subpackages/info/flowchart/flowchart.wxml"] = $gwx2_XC_1(
    "./subpackages/info/flowchart/flowchart.wxml"
  );

var noCss =
  typeof __vd_version_info__ !== "undefined" &&
  __vd_version_info__.noCss === true;
if (!noCss) {
  __wxAppCode__["subpackages/info/flowchart/flowchart.wxss"] = setCssToHead(
    [
      ".",
      [1],
      "container.",
      [1],
      "data-v-118080e0{background-color:#e6edff;height:180vh;padding:20px}\n.",
      [1],
      "flowchart-title.",
      [1],
      "data-v-118080e0{background-color:#007aff;border:2px solid #0056b3;border-radius:5px;color:#fff;font-size:24px;font-weight:700;margin-bottom:20px;padding:10px;text-align:center}\n.",
      [1],
      "flowchart-steps.",
      [1],
      "data-v-118080e0{-webkit-flex-wrap:wrap;flex-wrap:wrap;-webkit-justify-content:center;justify-content:center}\n.",
      [1],
      "flowchart-steps.",
      [1],
      "data-v-118080e0,.",
      [1],
      "step.",
      [1],
      "data-v-118080e0{-webkit-align-items:center;align-items:center;display:-webkit-flex;display:flex}\n.",
      [1],
      "step.",
      [1],
      "data-v-118080e0{-webkit-flex-direction:column;flex-direction:column;margin:10px}\n.",
      [1],
      "step-number.",
      [1],
      "data-v-118080e0{-webkit-align-items:center;align-items:center;background-color:#007aff;border-radius:50%;color:#fff;display:-webkit-flex;display:flex;font-size:16px;font-weight:700;height:30px;-webkit-justify-content:center;justify-content:center;margin-bottom:5px;width:30px}\n.",
      [1],
      "step-content.",
      [1],
      "data-v-118080e0{background-color:#a5b9fc;border-radius:5px;color:#1b1b4b;padding:10px;text-align:center}\n.",
      [1],
      "step-arrow.",
      [1],
      "data-v-118080e0{font-size:24px;margin:0 10px}\n.",
      [1],
      "warm-tip.",
      [1],
      "data-v-118080e0{border:2px solid #007aff;border-radius:5px;margin-top:20px;padding:10px}\n.",
      [1],
      "tip-title.",
      [1],
      "data-v-118080e0{font-weight:700}\n.",
      [1],
      "tip-content.",
      [1],
      "data-v-118080e0{color:#1b1b4b}\n",
    ],
    undefined,
    { path: "./subpackages/info/flowchart/flowchart.wxss" }
  );
}

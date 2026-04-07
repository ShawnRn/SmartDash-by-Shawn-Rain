$gwx2_XC_3 = (function (
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
    var z = __WXML_GLOBAL__.ops_set.$gwx2_XC_3 || [];
    function gz$gwx2_XC_3_1() {
      if (__WXML_GLOBAL__.ops_cached.$gwx2_XC_3_1)
        return __WXML_GLOBAL__.ops_cached.$gwx2_XC_3_1;
      __WXML_GLOBAL__.ops_cached.$gwx2_XC_3_1 = [];
      (function (z) {
        var a = 11;
        function Z(ops) {
          z.push(ops);
        }
        Z([3, "faq-list-page data-v-0bb5771d"]);
        Z([3, "category-title data-v-0bb5771d"]);
        Z([
          a,
          [
            [2, "+"],
            [
              [2, "+"],
              [1, ""],
              [[7], [3, "categoryName"]],
            ],
            [1, ""],
          ],
        ]);
        Z([3, "category-description data-v-0bb5771d"]);
        Z([
          a,
          [
            [2, "+"],
            [
              [2, "+"],
              [1, ""],
              [[7], [3, "categoryDescription"]],
            ],
            [1, ""],
          ],
        ]);
        Z([3, "qIndex"]);
        Z([3, "question"]);
        Z([[7], [3, "currentQuestionList"]]);
        Z(z[5]);
        Z([3, "__e"]);
        Z([3, "question-item data-v-0bb5771d"]);
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
                          [[5], [1, "showSolution"]],
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
                                      [[5], [1, "currentQuestionList"]],
                                      [1, ""],
                                    ],
                                    [[7], [3, "qIndex"]],
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
              [[6], [[7], [3, "question"]], [3, "question"]],
            ],
            [1, ""],
          ],
        ]);
      })(__WXML_GLOBAL__.ops_cached.$gwx2_XC_3_1);
      return __WXML_GLOBAL__.ops_cached.$gwx2_XC_3_1;
    }
    __WXML_GLOBAL__.ops_set.$gwx2_XC_3 = z;
    __WXML_GLOBAL__.ops_init.$gwx2_XC_3 = true;
    var x = ["./subpackages/info/pages/faqlist/faqlist.wxml"];
    d_[x[0]] = {};
    var m0 = function (e, s, r, gg) {
      var z = gz$gwx2_XC_3_1();
      var xEF = _n("view");
      _rz(z, xEF, "class", 0, e, s, gg);
      var oFF = _n("view");
      _rz(z, oFF, "class", 1, e, s, gg);
      var fGF = _oz(z, 2, e, s, gg);
      _(oFF, fGF);
      _(xEF, oFF);
      var cHF = _n("view");
      _rz(z, cHF, "class", 3, e, s, gg);
      var hIF = _oz(z, 4, e, s, gg);
      _(cHF, hIF);
      _(xEF, cHF);
      var oJF = _v();
      _(xEF, oJF);
      var cKF = function (lMF, oLF, aNF, gg) {
        var ePF = _mz(
          z,
          "view",
          ["bindtap", 9, "class", 1, "data-event-opts", 2],
          [],
          lMF,
          oLF,
          gg
        );
        var bQF = _oz(z, 12, lMF, oLF, gg);
        _(ePF, bQF);
        _(aNF, ePF);
        return aNF;
      };
      oJF.wxXCkey = 2;
      _2z(z, 7, cKF, e, s, gg, oJF, "question", "qIndex", "qIndex");
      _(r, xEF);
      return r;
    };
    e_[x[0]] = { f: m0, j: [], i: [], ti: [], ic: [] };
    if (path && e_[path]) {
      outerGlobal.__wxml_comp_version__ = 0.02;
      return function (env, dd, global) {
        $gwxc = 0;
        var root = { tag: "wx-page" };
        root.children = [];
        g = "$gwx2_XC_3";
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
if (__vd_version_info__.delayedGwx || false) $gwx2_XC_3();
if (__vd_version_info__.delayedGwx)
  __wxAppCode__["subpackages/info/pages/faqlist/faqlist.wxml"] = [
    $gwx2_XC_3,
    "./subpackages/info/pages/faqlist/faqlist.wxml",
  ];
else
  __wxAppCode__["subpackages/info/pages/faqlist/faqlist.wxml"] = $gwx2_XC_3(
    "./subpackages/info/pages/faqlist/faqlist.wxml"
  );

var noCss =
  typeof __vd_version_info__ !== "undefined" &&
  __vd_version_info__.noCss === true;
if (!noCss) {
  __wxAppCode__["subpackages/info/pages/faqlist/faqlist.wxss"] = setCssToHead(
    [
      ".",
      [1],
      "faq-list-page.",
      [1],
      "data-v-0bb5771d{background-color:#e6edff;height:100vh;padding:20px}\n.",
      [1],
      "category-title.",
      [1],
      "data-v-0bb5771d{background-color:#636ef1;border-radius:8px;color:#fff;font-size:24px;font-weight:700;margin-bottom:5px;padding:15px;text-align:center}\n.",
      [1],
      "category-description.",
      [1],
      "data-v-0bb5771d{background-color:#a5b9fc;border-radius:8px;color:#333;font-size:16px;font-weight:700;line-height:1.5;margin-bottom:10px;padding:15px}\n.",
      [1],
      "question-item.",
      [1],
      "data-v-0bb5771d{background-color:#c7d5fe;border-bottom:1px solid #007bff;color:#1b1b4b;cursor:pointer;font-size:18px;padding:15px}\n.",
      [1],
      "question-item.",
      [1],
      "data-v-0bb5771d:last-child{border-bottom:none}\n",
    ],
    undefined,
    { path: "./subpackages/info/pages/faqlist/faqlist.wxss" }
  );
}

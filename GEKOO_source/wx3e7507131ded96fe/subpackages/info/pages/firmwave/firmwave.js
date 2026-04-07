require("../../common/vendor.js"),
  (global.webpackJsonp = global.webpackJsonp || []).push([
    ["subpackages/info/pages/firmwave/firmwave"],
    {
      164:
        /*!******************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"subpackages%2Finfo%2Fpages%2Ffirmwave%2Ffirmwave"} ***!
    \******************************************************************************************************************/
        /*! no static exports found */
        function (e, n, t) {
          (function (e, n) {
            var o = t(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
            t(/*! uni-pages */ 26);
            o(t(/*! vue */ 25));
            var d = o(
              t(/*! ./subpackages/info/pages/firmwave/firmwave.vue */ 165)
            );
            (e.__webpack_require_UNI_MP_PLUGIN__ = t), n(d.default);
          }).call(
            this,
            t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
              .default,
            t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
              .createPage
          );
        },
      165:
        /*!*******************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/firmwave/firmwave.vue ***!
    \*******************************************************************************************/
        /*! no static exports found */
        function (e, n, t) {
          t.r(n);
          var o = t(
              /*! ./firmwave.vue?vue&type=template&id=213955d3&scoped=true& */ 166
            ),
            d = t(/*! ./firmwave.vue?vue&type=script&lang=js& */ 168);
          for (var r in d)
            ["default"].indexOf(r) < 0 &&
              (function (e) {
                t.d(n, e, function () {
                  return d[e];
                });
              })(r);
          t(
            /*! ./firmwave.vue?vue&type=style&index=0&id=213955d3&scoped=true&lang=css& */ 170
          );
          var a = t(
              /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
            ),
            i = Object(a.default)(
              d.default,
              o.render,
              o.staticRenderFns,
              !1,
              null,
              "213955d3",
              null,
              !1,
              o.components,
              void 0
            );
          (i.options.__file = "subpackages/info/pages/firmwave/firmwave.vue"),
            (n.default = i.exports);
        },
      166:
        /*!**************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/firmwave/firmwave.vue?vue&type=template&id=213955d3&scoped=true& ***!
    \**************************************************************************************************************************************/
        /*! exports provided: render, staticRenderFns, recyclableRender, components */
        function (e, n, t) {
          t.r(n);
          var o = t(
            /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./firmwave.vue?vue&type=template&id=213955d3&scoped=true& */ 167
          );
          t.d(n, "render", function () {
            return o.render;
          }),
            t.d(n, "staticRenderFns", function () {
              return o.staticRenderFns;
            }),
            t.d(n, "recyclableRender", function () {
              return o.recyclableRender;
            }),
            t.d(n, "components", function () {
              return o.components;
            });
        },
      167:
        /*!**************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/firmwave/firmwave.vue?vue&type=template&id=213955d3&scoped=true& ***!
    \**************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
        /*! exports provided: render, staticRenderFns, recyclableRender, components */
        function (e, n, t) {
          t.r(n),
            t.d(n, "render", function () {
              return o;
            }),
            t.d(n, "staticRenderFns", function () {
              return r;
            }),
            t.d(n, "recyclableRender", function () {
              return d;
            }),
            t.d(n, "components", function () {});
          var o = function () {
              var e = this,
                n = e.$createElement,
                t =
                  (e._self._c,
                  e.__map(e.firmwareSeries, function (n, t) {
                    return {
                      $orig: e.__get_orig(n),
                      l0: n.isExpanded
                        ? e.__map(n.updates, function (n, t) {
                            return {
                              $orig: e.__get_orig(n),
                              g0: n.models.join("、"),
                            };
                          })
                        : null,
                    };
                  }));
              e.$mp.data = Object.assign({}, { $root: { l1: t } });
            },
            d = !1,
            r = [];
          o._withStripped = !0;
        },
      168:
        /*!********************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/firmwave/firmwave.vue?vue&type=script&lang=js& ***!
    \********************************************************************************************************************/
        /*! no static exports found */
        function (e, n, t) {
          t.r(n);
          var o = t(
              /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./firmwave.vue?vue&type=script&lang=js& */ 169
            ),
            d = t.n(o);
          for (var r in o)
            ["default"].indexOf(r) < 0 &&
              (function (e) {
                t.d(n, e, function () {
                  return o[e];
                });
              })(r);
          n.default = d.a;
        },
      169:
        /*!***************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/firmwave/firmwave.vue?vue&type=script&lang=js& ***!
    \***************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
        /*! no static exports found */
        function (e, n, t) {
          (function (e) {
            Object.defineProperty(n, "__esModule", { value: !0 }),
              (n.default = void 0);
            var o = t(/*! @/utils/i18n.js */ 40),
              d = {
                computed: {
                  $t: function () {
                    return o.t;
                  },
                },
                data: function () {
                  return {
                    bannerImage: "/static/firmware-banner.png",
                    firmwareSeries: [
                      {
                        code: "0",
                        name: "GK系列",
                        isExpanded: !1,
                        updates: [
                          {
                            version: "v26",
                            date: "2025-4-22",
                            content: [
                              "新增可设置刹车是否断电",
                              "细化控制器的功能状态和挡位状态",
                              "新增陡坡缓降",
                              "优化TCS/EBS性能",
                            ],
                            models: ["全系列"],
                          },
                          {
                            version: "v24",
                            date: "2024-9-26",
                            content: ["新增骑行模式", "常规优化"],
                            models: ["全系列"],
                          },
                        ],
                      },
                      {
                        code: "4",
                        name: "XM系列",
                        isExpanded: !1,
                        updates: [
                          {
                            version: "v26",
                            date: "2025-4-22",
                            content: [
                              "新增可设置刹车是否断电",
                              "细化控制器的功能状态和挡位状态",
                              "新增陡坡缓降",
                              "优化TCS/EBS性能",
                            ],
                            models: ["全系列"],
                          },
                          {
                            version: "v25",
                            date: "2024-9-26",
                            content: ["新增e80cmk2", "改进了用户界面"],
                            models: ["260", "450", "550p"],
                          },
                        ],
                      },
                      {
                        code: "8",
                        name: "ND系列",
                        isExpanded: !1,
                        updates: [
                          {
                            version: "v26",
                            date: "2026-2-09",
                            content: ["内置协议优化"],
                            models: ["全系列"],
                          },
                          {
                            version: "v26",
                            date: "2025-10-21",
                            content: ["新增车型FX"],
                            models: ["全系列"],
                          },
                          {
                            version: "v26",
                            date: "2025-4-22",
                            content: ["新增陡坡缓降", "优化TCS/EBS性能"],
                            models: ["全系列"],
                          },
                          {
                            version: "v26",
                            date: "2025-3-24/28",
                            content: [
                              "发布26号固件",
                              "一线通支持车型FX Play Pro",
                            ],
                            models: ["260,350,450,550,550P,850"],
                          },
                          {
                            version: "v25",
                            date: "2025-1-12",
                            content: ["优化极核车型"],
                            models: ["全系列"],
                          },
                        ],
                      },
                      {
                        code: "3",
                        name: "AE系列",
                        isExpanded: !1,
                        updates: [
                          {
                            version: "v26",
                            date: "2026-3-31",
                            content: ["优化坐垫感应功能"],
                            models: ["全系列"],
                          },
                          {
                            version: "v26",
                            date: "2025-7-08",
                            content: ["优化锁电机功能", "逻辑优化"],
                            models: ["全系列"],
                          },
                          {
                            version: "v26",
                            date: "2025-4-22",
                            content: [
                              "优化陡坡缓降",
                              "优化新AE4的定速巡航显示",
                            ],
                            models: ["全系列"],
                          },
                          {
                            version: "v26",
                            date: "2025-4-22",
                            content: [
                              "优化陡坡缓降",
                              "优化新AE4的定速巡航显示",
                            ],
                            models: ["全系列"],
                          },
                          {
                            version: "v26",
                            date: "2025-4-12",
                            content: ["修复24款AE4适配问题", "优化TCS和EBS"],
                            models: ["全系列"],
                          },
                          {
                            version: "v26",
                            date: "2025-4-10",
                            content: [
                              "新增可设置刹车是否断电",
                              "优化AE5适配",
                              "支持新款AE4",
                              "新增陡坡缓降",
                              "优化Tcs性能",
                              "增加BMS协议支持",
                              "增加边撑有效开关",
                            ],
                            models: ["全系列"],
                          },
                          {
                            version: "v25",
                            date: "2025-1-12",
                            content: ["优化极核车型"],
                            models: ["全系列"],
                          },
                        ],
                      },
                      {
                        code: "7",
                        name: "CP系列",
                        isExpanded: !1,
                        updates: [
                          {
                            version: "v26",
                            date: "2026-3-31",
                            content: ["优化SQ的相关功能适配"],
                            models: ["全系列"],
                          },
                          {
                            version: "v26",
                            date: "2025-10-09",
                            content: ["优化一二挡转把", "增加适配性"],
                            models: ["全系列"],
                          },
                          {
                            version: "v26",
                            date: "2025-4-22",
                            content: [
                              "新增可设置刹车是否断电",
                              "细化控制器的功能状态和挡位状态",
                              "新增陡坡缓降",
                              "优化TCS/EBS性能",
                            ],
                            models: ["全系列"],
                          },
                          {
                            version: "v25",
                            date: "2025-1-12",
                            content: ["初始版本"],
                            models: ["全系列"],
                          },
                        ],
                      },
                      {
                        code: "6",
                        name: "PD系列",
                        isExpanded: !1,
                        updates: [
                          {
                            version: "v26",
                            date: "2025-4-22",
                            content: [
                              "新增可设置刹车是否断电",
                              "细化控制器的功能状态和挡位状态",
                              "新增陡坡缓降",
                              "优化TCS/EBS性能",
                            ],
                            models: ["全系列"],
                          },
                          {
                            version: "v25",
                            date: "2024-8-21",
                            content: ["常规优化"],
                            models: ["全系列"],
                          },
                        ],
                      },
                      {
                        code: "5",
                        name: "UB系列",
                        isExpanded: !1,
                        updates: [
                          {
                            version: "v26",
                            date: "2026-1-18",
                            content: ["增加适配性"],
                            models: ["全系列"],
                          },
                          {
                            version: "v26",
                            date: "2025-4-22",
                            content: [
                              "新增可设置刹车是否断电",
                              "细化控制器的功能状态和挡位状态",
                              "新增陡坡缓降",
                              "优化TCS/EBS性能",
                            ],
                            models: ["全系列"],
                          },
                          {
                            version: "v25",
                            date: "2024-8-21",
                            content: ["常规优化"],
                            models: ["全系列"],
                          },
                        ],
                      },
                    ],
                  };
                },
                methods: {
                  toggleSeries: function (e) {
                    this.firmwareSeries[e].isExpanded =
                      !this.firmwareSeries[e].isExpanded;
                  },
                },
                onLoad: function (n) {
                  var t = n.code;
                  if ((console.log("options:" + t), t)) {
                    var o = this.firmwareSeries.find(function (e) {
                      return e.code === t;
                    });
                    o && (o.isExpanded = !0);
                  }
                  e.setNavigationBarTitle({
                    title: this.$t("home_page.grid.item6"),
                  });
                },
              };
            n.default = d;
          }).call(
            this,
            t(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
              .default
          );
        },
      170:
        /*!****************************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/firmwave/firmwave.vue?vue&type=style&index=0&id=213955d3&scoped=true&lang=css& ***!
    \****************************************************************************************************************************************************/
        /*! no static exports found */
        function (e, n, t) {
          t.r(n);
          var o = t(
              /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./firmwave.vue?vue&type=style&index=0&id=213955d3&scoped=true&lang=css& */ 171
            ),
            d = t.n(o);
          for (var r in o)
            ["default"].indexOf(r) < 0 &&
              (function (e) {
                t.d(n, e, function () {
                  return o[e];
                });
              })(r);
          n.default = d.a;
        },
      171:
        /*!********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--6-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--6-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--6-oneOf-1-2!./node_modules/postcss-loader/src??ref--6-oneOf-1-3!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/subpackages/info/pages/firmwave/firmwave.vue?vue&type=style&index=0&id=213955d3&scoped=true&lang=css& ***!
    \********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
        /*! no static exports found */
        function (e, n, t) {},
    },
    [[164, "common/runtime", "common/vendor"]],
  ]);

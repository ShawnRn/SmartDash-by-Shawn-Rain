require("../../@babel/runtime/helpers/Arrayincludes"),
  (global.webpackJsonp = global.webpackJsonp || []).push([
    ["pages/ble_debug/ble_debug"],
    {
      59:
        /*!***********************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/main.js?{"page":"pages%2Fble_debug%2Fble_debug"} ***!
    \***********************************************************************************************/
        /*! no static exports found */
        function (e, t, a) {
          (function (e, t) {
            var n = a(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
            a(/*! uni-pages */ 26);
            n(a(/*! vue */ 25));
            var r = n(a(/*! ./pages/ble_debug/ble_debug.vue */ 60));
            (e.__webpack_require_UNI_MP_PLUGIN__ = a), t(r.default);
          }).call(
            this,
            a(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
              .default,
            a(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
              .createPage
          );
        },
      60:
        /*!****************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/ble_debug/ble_debug.vue ***!
    \****************************************************************************/
        /*! no static exports found */
        function (e, t, a) {
          a.r(t);
          var n = a(
              /*! ./ble_debug.vue?vue&type=template&id=0f056466&scoped=true& */ 61
            ),
            r = a(/*! ./ble_debug.vue?vue&type=script&lang=js& */ 63);
          for (var i in r)
            ["default"].indexOf(i) < 0 &&
              (function (e) {
                a.d(t, e, function () {
                  return r[e];
                });
              })(i);
          a(
            /*! ./ble_debug.vue?vue&type=style&index=0&id=0f056466&lang=less&scoped=true& */ 71
          );
          var o = a(
              /*! ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 32
            ),
            s = Object(o.default)(
              r.default,
              n.render,
              n.staticRenderFns,
              !1,
              null,
              "0f056466",
              null,
              !1,
              n.components,
              void 0
            );
          (s.options.__file = "pages/ble_debug/ble_debug.vue"),
            (t.default = s.exports);
        },
      61:
        /*!***********************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/ble_debug/ble_debug.vue?vue&type=template&id=0f056466&scoped=true& ***!
    \***********************************************************************************************************************/
        /*! exports provided: render, staticRenderFns, recyclableRender, components */
        function (e, t, a) {
          a.r(t);
          var n = a(
            /*! -!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./ble_debug.vue?vue&type=template&id=0f056466&scoped=true& */ 62
          );
          a.d(t, "render", function () {
            return n.render;
          }),
            a.d(t, "staticRenderFns", function () {
              return n.staticRenderFns;
            }),
            a.d(t, "recyclableRender", function () {
              return n.recyclableRender;
            }),
            a.d(t, "components", function () {
              return n.components;
            });
        },
      62:
        /*!***********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/ble_debug/ble_debug.vue?vue&type=template&id=0f056466&scoped=true& ***!
    \***********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
        /*! exports provided: render, staticRenderFns, recyclableRender, components */
        function (e, t, a) {
          var n;
          a.r(t),
            a.d(t, "render", function () {
              return r;
            }),
            a.d(t, "staticRenderFns", function () {
              return o;
            }),
            a.d(t, "recyclableRender", function () {
              return i;
            }),
            a.d(t, "components", function () {
              return n;
            });
          try {
            n = {
              TableModal: function () {
                return a
                  .e(
                    /*! import() | components/TableModal/TableModal */ "components/TableModal/TableModal"
                  )
                  .then(
                    a.bind(
                      null,
                      /*! @/components/TableModal/TableModal.vue */ 270
                    )
                  );
              },
            };
          } catch (e) {
            if (
              -1 === e.message.indexOf("Cannot find module") ||
              -1 === e.message.indexOf(".vue")
            )
              throw e;
            console.error(e.message),
              console.error("1. 排查组件名称拼写是否正确"),
              console.error(
                "2. 排查组件是否符合 easycom 规范，文档：https://uniapp.dcloud.net.cn/collocation/pages?id=easycom"
              ),
              console.error(
                "3. 若组件不符合 easycom 规范，需手动引入，并在 components 中注册该组件"
              );
          }
          var r = function () {
              var e = this,
                t = e.$createElement,
                a = (e._self._c, e.$t("ble_page.tabbar.realtime")),
                n = e.$t("ble_page.tabbar.parameter"),
                r = e.$t("ble_page.tabbar.firmwave"),
                i = e.$t("ble_page.tabbar.assistant"),
                o =
                  0 === e.TabCur &&
                  e.Amplify &&
                  e.single_soft_ver >= 26 &&
                  e.single_soft_ver <= 99
                    ? e.__map(e.blockTexts, function (t, a) {
                        return {
                          $orig: e.__get_orig(t),
                          m4: e.getBlockGradient(a),
                        };
                      })
                    : null,
                s =
                  0 !== e.TabCur || e.Amplify
                    ? null
                    : e.$t("ble_page.card1.vol"),
                c =
                  0 !== e.TabCur || e.Amplify
                    ? null
                    : e.$t("ble_page.card1.cur"),
                l =
                  0 !== e.TabCur || e.Amplify
                    ? null
                    : e.$t("ble_page.card1.velo"),
                u =
                  0 !== e.TabCur || e.Amplify
                    ? null
                    : e.$t("ble_page.card1.temp"),
                _ =
                  0 !== e.TabCur || e.Amplify
                    ? null
                    : e.$t("ble_page.card1.vain"),
                d =
                  0 !== e.TabCur || e.Amplify
                    ? null
                    : e.$t("ble_page.card1.error"),
                h =
                  0 === e.TabCur &&
                  !e.Amplify &&
                  e.debug_buffer_flag &&
                  e.debug_data
                    ? e.__map(e.debug_buffer, function (t, a) {
                        return {
                          $orig: e.__get_orig(t),
                          m11: e.formatDecimal(a, t),
                          m12: e.formatHexadecimal(t),
                        };
                      })
                    : null,
                f =
                  0 !== e.TabCur || e.Amplify
                    ? null
                    : e.$t("ble_page.card1.btn_learn"),
                p =
                  0 !== e.TabCur || e.Amplify
                    ? null
                    : e.$t("ble_page.card1.btn_check"),
                m =
                  0 !== e.TabCur || e.Amplify
                    ? null
                    : e.$t("ble_page.card1.btn_error"),
                g =
                  0 !== e.TabCur || e.Amplify
                    ? null
                    : e.$t("ble_page.card1.btn_switch"),
                b =
                  0 !== e.TabCur || e.Amplify
                    ? null
                    : e.$t("ble_page.card1.btn_warn"),
                v =
                  0 !== e.TabCur || e.Amplify
                    ? null
                    : e.$t("ble_page.card1.btn_level"),
                w = 0 !== e.TabCur || e.Amplify ? null : e.$t("ble_page.tip"),
                T =
                  0 !== e.TabCur && 1 === e.TabCur
                    ? e.__map(e.parameter_title, function (t, a) {
                        return {
                          $orig: e.__get_orig(t),
                          l2: t.visible
                            ? e.__map(
                                e.parameter_setting[t.id],
                                function (t, a) {
                                  var n = e.__get_orig(t),
                                    r =
                                      ([
                                        "int",
                                        "uint",
                                        "bool",
                                        "float",
                                        "text",
                                        "picker",
                                      ].includes(t.type) &&
                                        (!t.ver ||
                                          e.single_soft_ver >= t.ver)) ||
                                      ("list" === t.type &&
                                        (!t.ver ||
                                          e.single_soft_ver >= t.ver) &&
                                        t.options &&
                                        t.options.length > 0),
                                    i = r
                                      ? ["int", "uint", "float"].includes(
                                          t.type
                                        )
                                      : null;
                                  return {
                                    $orig: n,
                                    g0: r,
                                    g1: i,
                                    g2:
                                      r && !i && "bool" !== t.type
                                        ? "list" === t.type &&
                                          t.options &&
                                          t.options.length > 0
                                        : null,
                                    g3:
                                      ([
                                        "int",
                                        "uint",
                                        "bool",
                                        "float",
                                        "text",
                                        "picker",
                                      ].includes(t.type) &&
                                        (!t.ver ||
                                          e.single_soft_ver >= t.ver)) ||
                                      ("list" === t.type &&
                                        (!t.ver ||
                                          e.single_soft_ver >= t.ver) &&
                                        t.options &&
                                        t.options.length > 0),
                                  };
                                }
                              )
                            : null,
                          g4: t.visible
                            ? e.parameter_tip_enable && t.buttons.length > 0
                            : null,
                        };
                      })
                    : null,
                C =
                  0 !== e.TabCur && 1 === e.TabCur
                    ? e.$t("ble_page.operationpanel")
                    : null;
              e._isMounted ||
                ((e.e0 = function (t, a) {
                  var n;
                  a = (
                    (n = arguments[arguments.length - 1].currentTarget.dataset)
                      .eventParams || n["event-params"]
                  ).index;
                  e.parameter_title[a].isarrow = t;
                }),
                (e.e1 = function (t) {
                  e.isTableModalShow = !1;
                })),
                (e.$mp.data = Object.assign(
                  {},
                  {
                    $root: {
                      m0: a,
                      m1: n,
                      m2: r,
                      m3: i,
                      l0: o,
                      m5: s,
                      m6: c,
                      m7: l,
                      m8: u,
                      m9: _,
                      m10: d,
                      l1: h,
                      m13: f,
                      m14: p,
                      m15: m,
                      m16: g,
                      m17: b,
                      m18: v,
                      m19: w,
                      l3: T,
                      m20: C,
                    },
                  }
                ));
            },
            i = !1,
            o = [];
          r._withStripped = !0;
        },
      63:
        /*!*****************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/ble_debug/ble_debug.vue?vue&type=script&lang=js& ***!
    \*****************************************************************************************************/
        /*! no static exports found */
        function (e, t, a) {
          a.r(t);
          var n = a(
              /*! -!./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./ble_debug.vue?vue&type=script&lang=js& */ 64
            ),
            r = a.n(n);
          for (var i in n)
            ["default"].indexOf(i) < 0 &&
              (function (e) {
                a.d(t, e, function () {
                  return n[e];
                });
              })(i);
          t.default = r.a;
        },
      64:
        /*!************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/ble_debug/ble_debug.vue?vue&type=script&lang=js& ***!
    \************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
        /*! no static exports found */
        function (e, t, a) {
          (function (e, n) {
            var r = a(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
            Object.defineProperty(t, "__esModule", { value: !0 }),
              (t.default = void 0);
            var i = r(a(/*! @babel/runtime/regenerator */ 53)),
              o = r(a(/*! @babel/runtime/helpers/asyncToGenerator */ 55)),
              s = a(/*! @/utils/i18n.js */ 40),
              c = a(/*! @/utils/parameter_list.js */ 65),
              l = a(/*! @/utils/parametertable.js */ 67),
              u = r(a(/*! @/utils/bluetooth.js */ 68)),
              _ = a(/*! @/utils/realtimedata.js */ 69),
              d = a(/*! @/utils/firmwave.js */ 70),
              h = a(/*! @/utils/crypto-util.js */ 56),
              f = [
                { id: 0, index: 5, arrow: !1 },
                { id: 1, index: 1, arrow: !0 },
                { id: 2, index: 0, arrow: !0 },
                { id: 3, index: 2, arrow: !1 },
                { id: 4, index: 6, arrow: !1 },
                { id: 5, index: 8, arrow: !1 },
              ],
              p = !1,
              m = !1,
              g = !1,
              b = !1,
              v = { dev: -1, user: 8888 },
              w = 0,
              T = !1,
              C = !1,
              x = 0,
              k = !1,
              y = 4,
              M = 0,
              A = null,
              D = 0,
              S = 0,
              $ = !1,
              B = 0,
              F = !1,
              I = null,
              P = null,
              L = null,
              V = null,
              R = null,
              q = null,
              N = null,
              O = {
                components: {
                  Module_Text: function () {
                    a.e(
                      /*! require.ensure | components/module_text/module_text */ "components/module_text/module_text"
                    )
                      .then(
                        function () {
                          return resolve(
                            a(
                              /*! @/components/module_text/module_text.vue */ 284
                            )
                          );
                        }.bind(null, a)
                      )
                      .catch(a.oe);
                  },
                  ItemCard: function () {
                    a.e(
                      /*! require.ensure | components/item_card/item_card */ "components/item_card/item_card"
                    )
                      .then(
                        function () {
                          return resolve(
                            a(/*! @/components/item_card/item_card.vue */ 291)
                          );
                        }.bind(null, a)
                      )
                      .catch(a.oe);
                  },
                  CarDashBoard: function () {
                    a.e(
                      /*! require.ensure | components/dashboard/zui-meter-basic */ "components/dashboard/zui-meter-basic"
                    )
                      .then(
                        function () {
                          return resolve(
                            a(
                              /*! @/components/dashboard/zui-meter-basic.vue */ 277
                            )
                          );
                        }.bind(null, a)
                      )
                      .catch(a.oe);
                  },
                  ParamGroupCard: function () {
                    a.e(
                      /*! require.ensure | components/text_group/text_group */ "components/text_group/text_group"
                    )
                      .then(
                        function () {
                          return resolve(
                            a(/*! @/components/text_group/text_group.vue */ 298)
                          );
                        }.bind(null, a)
                      )
                      .catch(a.oe);
                  },
                  FirmwareUpgrade: function () {
                    a.e(
                      /*! require.ensure | components/firmwave_update/firmwave_update */ "components/firmwave_update/firmwave_update"
                    )
                      .then(
                        function () {
                          return resolve(
                            a(
                              /*! @/components/firmwave_update/firmwave_update.vue */ 305
                            )
                          );
                        }.bind(null, a)
                      )
                      .catch(a.oe);
                  },
                  DebugComponent: function () {
                    a.e(
                      /*! require.ensure | components/debugger/debugger */ "components/debugger/debugger"
                    )
                      .then(
                        function () {
                          return resolve(
                            a(/*! @/components/debugger/debugger.vue */ 312)
                          );
                        }.bind(null, a)
                      )
                      .catch(a.oe);
                  },
                  StatusContainer: function () {
                    a.e(
                      /*! require.ensure | components/blestatus/blestatus */ "components/blestatus/blestatus"
                    )
                      .then(
                        function () {
                          return resolve(
                            a(/*! @/components/blestatus/blestatus.vue */ 319)
                          );
                        }.bind(null, a)
                      )
                      .catch(a.oe);
                  },
                  TableModal: function () {
                    a.e(
                      /*! require.ensure | components/TableModal/TableModal */ "components/TableModal/TableModal"
                    )
                      .then(
                        function () {
                          return resolve(
                            a(/*! @/components/TableModal/TableModal.vue */ 270)
                          );
                        }.bind(null, a)
                      )
                      .catch(a.oe);
                  },
                },
                computed: {
                  $t: function () {
                    return s.t;
                  },
                },
                created: function () {
                  (this.realtime_page_data.display = _.realtimedata),
                    (this.realtime_page_data.gear_io_info = _.bitConfig);
                },
                mounted: function () {
                  (this.parameter_title = (0, c.getparameter_title)()),
                    (this.parameter_setting = (0, l.getParameterSetting)()),
                    (this.realtime_page_data.identity = (0, c.getIdentity)()),
                    (this.parameter_sheet = (0, c.getparameter_sheet)());
                },
                data: function () {
                  return {
                    debug_buffer_flag: !1,
                    isCollapsed2: !0,
                    isCollapsed: !1,
                    debug_buffer: [],
                    debug_buffer_name: [
                      "转矩限制",
                      "转矩命令",
                      "油门输入",
                      "转矩请求",
                      "Theta角",
                      "角度偏差",
                      "电机转速",
                      "反馈转矩",
                      "母线电压",
                      "母线电流",
                      "相线电流",
                      "相线电压",
                      "保留",
                      "保留",
                      "保留",
                      "保留",
                      "5V电压",
                      "12V电压",
                      "温度",
                      "反馈状态",
                      "系统模式",
                      "IO口",
                      "ERROR",
                      "保留",
                      "保留",
                      "保留",
                      "保留",
                      "保留",
                      "保留",
                      "软件版本",
                      "参数版本",
                    ],
                    showBubbleHint: !0,
                    debug_data: !1,
                    inputKey: "",
                    prog_progess: 0,
                    Amplify: !1,
                    key_time: 0,
                    key_crc: 0,
                    TabCur: 0,
                    isConnecting: !1,
                    ble_isnew: !1,
                    ble_connect: !1,
                    ble_r_ok: !1,
                    errorMsg: "未连接",
                    connect_device: null,
                    dataBuffer: [],
                    timer: null,
                    timeoutDuration: 3e3,
                    read_flag: !1,
                    targetData: 1,
                    blockTexts: [
                      "防盗",
                      "巡航",
                      "驻车",
                      "EBS",
                      "助力",
                      "TCS",
                      "GB",
                    ],
                    realtime_page_data: {
                      serial_code: 0,
                      m_debug: "GK550PX",
                      isNew: !1,
                      isLock: !1,
                      identity: [],
                      display: [],
                      gear_io_info: [],
                      speed: 0,
                      velocity: 0,
                      timer: null,
                      status: "停机",
                      func_status: "",
                      speed_zoom: 0,
                      voltage_zoom: 0,
                    },
                    isTableModalShow: !1,
                    currentTableData: [],
                    currentTableHeaders: [],
                    buttonDataMap: c.buttonDataMap,
                    parameter_tip_enable: !0,
                    parameter_title: [],
                    parameter_setting: [],
                    parameter_read: [],
                    parameter_write: [],
                    parameter_sheet: [],
                    single_soft_ver: 25,
                    hard_ver: 0,
                    currentFirmwareVersion: "待读取",
                    targetFirmwareVersion: "待读取",
                    manufacturerFirmwareId: 0,
                    targetFirmwareCode: null,
                    polePairs: 8.23,
                    speed: 0,
                    speed_max: 60,
                    speed_calc_cnt: 0,
                    calc_lock: !1,
                    idleVoltage: 0.5,
                    fullVoltage: 4.5,
                    busCurrentValue: 80,
                    phaseCurrentValue: 260,
                    diameter: 40,
                  };
                },
                watch: {
                  TabCur: function (t) {
                    var a = this;
                    0 === t && this.ble_connect
                      ? (this.start_realtime_update(),
                        (this.read_flag = !1),
                        setTimeout(function () {
                          b = !0;
                        }, 500))
                      : (this.stop_realtime_update(),
                        this.ble_connect &&
                          0 == this.read_flag &&
                          (setTimeout(function () {
                            a.writeData("AA110001");
                          }, 300),
                          setTimeout(function () {
                            if (0 == a.read_flag) {
                              if (0 == a.TabCur) return;
                              e.showModal({
                                title: "温馨提示",
                                content:
                                  "检测到自动读取参数失败,请手动读取一下参数",
                                success: function (e) {
                                  e.confirm
                                    ? a.writeData("AA110001")
                                    : e.cancel;
                                },
                              });
                            }
                          }, 3e3)));
                  },
                  ble_connect: function (e) {
                    var t = this;
                    e && 0 === this.TabCur
                      ? setTimeout(function () {
                          t.start_realtime_update();
                        }, 1e3)
                      : this.stop_realtime_update();
                  },
                },
                methods: {
                  toggleCollapse2: function () {
                    this.isCollapsed2 = !this.isCollapsed2;
                  },
                  toggleCollapse: function () {
                    this.isCollapsed = !this.isCollapsed;
                  },
                  formatDecimal: function (e, t) {
                    var a =
                      t /
                      [
                        163.84, 163.84, 2450, 163.84, 182, 182, 5.46, 163.84,
                        273.0666667, 1, 32.768, 273.0666667, 1, 1, 1, 1,
                        620.6060606, 146.0249554, 100, 1, 1, 1, 1, 1, 1, 1, 1,
                        1, 1, 1, 1, 1, 1,
                      ][e];
                    return Math.abs(a - Math.round(a)) < 0.001
                      ? Math.round(a).toString()
                      : a.toFixed(1).toString();
                  },
                  formatHexadecimal: function (e) {
                    return "0x" + e.toString(16).toUpperCase().padStart(4, "0");
                  },
                  decrypt: function (t) {
                    return (0, o.default)(
                      i.default.mark(function a() {
                        var n;
                        return i.default.wrap(
                          function (a) {
                            for (;;)
                              switch ((a.prev = a.next)) {
                                case 0:
                                  return (
                                    (a.prev = 0),
                                    (a.next = 3),
                                    (0, h.decryptData)("wosxq1990212gekoo", t)
                                  );
                                case 3:
                                  if (!(n = a.sent)) {
                                    a.next = 8;
                                    break;
                                  }
                                  return a.abrupt("return", {
                                    num1: n.num1,
                                    num2: n.num2,
                                  });
                                case 8:
                                  e.showToast({
                                    title: "指令失效",
                                    icon: "exception",
                                    duration: 2e3,
                                  });
                                case 9:
                                  a.next = 13;
                                  break;
                                case 11:
                                  (a.prev = 11), (a.t0 = a.catch(0));
                                case 13:
                                case "end":
                                  return a.stop();
                              }
                          },
                          a,
                          null,
                          [[0, 11]]
                        );
                      })
                    )();
                  },
                  fetchModule: function () {
                    var e = this;
                    return (0, o.default)(
                      i.default.mark(function t() {
                        var a;
                        return i.default.wrap(
                          function (t) {
                            for (;;)
                              switch ((t.prev = t.next)) {
                                case 0:
                                  return (
                                    (t.prev = 0),
                                    (t.next = 3),
                                    loadSubModuleAsync(
                                      "subpackages/firmwave_xm",
                                      "src/test.js"
                                    )
                                  );
                                case 3:
                                  (a = t.sent),
                                    (e.gk_hex_src = a),
                                    (t.next = 10);
                                  break;
                                case 7:
                                  (t.prev = 7),
                                    (t.t0 = t.catch(0)),
                                    console.error(
                                      "路径: "
                                        .concat(t.t0.mod, ", 错误: ")
                                        .concat(t.t0.errMsg)
                                    );
                                case 10:
                                case "end":
                                  return t.stop();
                              }
                          },
                          t,
                          null,
                          [[0, 7]]
                        );
                      })
                    )();
                  },
                  getBlockGradient: function (e) {
                    return 0 === ((this.targetData >> e) & 1)
                      ? "linear-gradient(135deg, #0056b3, #636ef1)"
                      : "linear-gradient(135deg, #00FF00, #008000)";
                  },
                  switch_display: function (e) {
                    this.Amplify = 0 != e;
                  },
                  tabSelect: function (e) {
                    (this.TabCur = Number(e.currentTarget.dataset.id)),
                      console.log(this.TabCur);
                  },
                  ebsCloseSet: function () {
                    e.navigateTo({
                      url: "/pages/webview/webview?url=".concat(
                        encodeURIComponent(
                          "https://mp.weixin.qq.com/s/IGnRHvwISFuYoJJQPXizVg"
                        )
                      ),
                    });
                  },
                  ebsCommondSet: function () {
                    e.navigateTo({
                      url: "/pages/webview/webview?url=".concat(
                        encodeURIComponent(
                          "https://mp.weixin.qq.com/s/4az8bu20sZiwj0kaRdb2FQ"
                        )
                      ),
                    });
                  },
                  handleVolSet: function () {
                    var t = this;
                    e.showModal({
                      title: "换电专用",
                      content:
                        "将电流和电压按照常用换电参数进行设置。母线50A,相线220A",
                      cancelText: "取消",
                      cancelColor: "#000000",
                      showCancel: !0,
                      confirmText: "是的",
                      confirmColor: "#3CC51F",
                      success: function (e) {
                        e.confirm
                          ? ((t.parameter_setting[0][0].value = 50),
                            (t.parameter_setting[0][1].value = 220),
                            (t.parameter_setting[8][0].value = 34),
                            (t.parameter_setting[8][1].value = 95),
                            (t.parameter_setting[8][2].value = 42),
                            (t.parameter_setting[8][3].value = 35),
                            t.writeData("AA210001"))
                          : e.cancel;
                      },
                      fail: function (e) {
                        console.log(e);
                      },
                    });
                  },
                  autoWriteCurrent: function (t) {
                    var a = this,
                      n = t[0].value,
                      r = t[1].value,
                      i = t[2].value,
                      o = t[3].value,
                      s = t[4].value,
                      c = t[5].value,
                      l = t[6].value;
                    e.showModal({
                      title: "修改参数提醒",
                      content: "确认要将计算结果写入控制器码",
                      cancelText: "取消",
                      cancelColor: "#000000",
                      showCancel: !0,
                      confirmText: "是的",
                      confirmColor: "#3CC51F",
                      success: function (e) {
                        e.confirm
                          ? ((a.parameter_setting[0][0].value = n),
                            (a.parameter_setting[0][1].value = r),
                            (a.parameter_setting[6][3].value = i),
                            (a.parameter_setting[8][0].value = o),
                            (a.parameter_setting[8][1].value = s),
                            (a.parameter_setting[8][2].value = c),
                            (a.parameter_setting[8][3].value = l),
                            a.writeData("AA210001"))
                          : e.cancel;
                      },
                      fail: function (e) {
                        console.log(e);
                      },
                    });
                  },
                  detectThrottleVoltage: function (t) {
                    var a = this;
                    0 == x && (this.start_realtime_update(), (x = 1)),
                      0 == t
                        ? e.showModal({
                            title: "空转把电压适配",
                            content: "请完全松开转把后点击确认",
                            cancelText: "取消",
                            cancelColor: "#000000",
                            showCancel: !0,
                            confirmText: "已经松开",
                            confirmColor: "#3CC51F",
                            success: function (e) {
                              e.confirm
                                ? ((a.idleVoltage = (
                                    a.realtime_page_data.display[4].value + 0.2
                                  ).toFixed(2)),
                                  (x = 0))
                                : e.cancel &&
                                  (a.stop_realtime_update(), (x = 0));
                            },
                            fail: function (e) {
                              console.log(e), a.stop_realtime_update(), (x = 0);
                            },
                          })
                        : e.showModal({
                            title: "满转把电压适配",
                            content: "请捏住刹车后，再把转把拧到底",
                            cancelText: "取消",
                            cancelColor: "#000000",
                            showCancel: !0,
                            confirmText: "已经到底",
                            confirmColor: "#3CC51F",
                            success: function (e) {
                              e.confirm
                                ? ((a.fullVoltage = (
                                    a.realtime_page_data.display[4].value + 0.2
                                  ).toFixed(2)),
                                  (x = 0))
                                : e.cancel &&
                                  (a.stop_realtime_update(), (x = 0));
                            },
                            fail: function (e) {
                              console.log(e), a.stop_realtime_update(), (x = 0);
                            },
                          });
                  },
                  adaptThrottle: function () {
                    e.navigateTo({
                      url: "/pages/webview/webview?url=".concat(
                        encodeURIComponent(
                          "https://mp.weixin.qq.com/s/Y3ZbzJMo1H8XzdRKHIoulA"
                        )
                      ),
                    });
                  },
                  handleTireRadiusChanged: function (e) {
                    console.log("接收到的轮胎半径值：", e);
                  },
                  handleAdaptPolePairs: function (e) {
                    (this.speed_max = 0),
                      (this.speed_calc_cnt = 0),
                      (this.calc_lock = !1),
                      this.get_location_speed();
                  },
                  exportParametersFile: function () {
                    var e = this.parameter_read;
                    (e[63] = e[61]),
                      (e[60] = this.debug_buffer[30]),
                      (e[61] = this.debug_buffer[26]),
                      (e[62] = this.debug_buffer[27]),
                      (0, c.exportAndChooseAction)(
                        this.parameter_setting,
                        this.parameter_read
                      );
                  },
                  importParametersFile: function () {
                    (0, c.loadAndUpdateData)(this.parameter_setting);
                  },
                  realtime_longclick_handle: function (t) {
                    switch (t.currentTarget.id) {
                      case "button_check":
                        var a = (0, _.getRecentPasswordRecords)();
                        if (0 === a.length)
                          e.showToast({ title: "暂无密码记录", icon: "none" });
                        else {
                          var n = a.join("\n");
                          e.showModal({
                            title: "最近 10 次密码记录",
                            content: n,
                            showCancel: !1,
                          });
                        }
                    }
                  },
                  check_ble_status: function (t, a) {
                    return 0 == this.ble_connect
                      ? (e.showToast({
                          title: "未建立连接",
                          icon: "error",
                          duration: 2e3,
                        }),
                        1)
                      : 1 == t && 0 == this.ble_r_ok
                      ? (e.showToast({
                          title: "通讯异常",
                          icon: "error",
                          duration: 2e3,
                        }),
                        2)
                      : 1 == a &&
                        this.single_soft_ver >= 19 &&
                        this.single_soft_ver <= 999 &&
                        0 == this.realtime_page_data.isLock
                      ? (e.showToast({
                          title: "蓝牙密码错误",
                          icon: "error",
                          duration: 2e3,
                        }),
                        3)
                      : 0;
                  },
                  realtime_click_handle: function (t) {
                    var a = t.currentTarget.id,
                      n = this;
                    switch (a) {
                      case "button_learn":
                        if (0 != this.check_ble_status(!0, !0)) return;
                        if (
                          0 != n.realtime_page_data.display[1].value ||
                          0 != n.realtime_page_data.display[2].value
                        )
                          return void e.showModal({
                            title: "提醒",
                            content:
                              "检测到控制器可能处在驱动模式下，当前工况下不允许自学习，请排查转把是否设置不合理或转把异常",
                            showCancel: !1,
                          });
                        e.showModal({
                          title: "提醒",
                          content: "确认要自学习吗",
                          cancelText: "我就看看",
                          cancelColor: "#000000",
                          showCancel: !0,
                          confirmText: "快快快快",
                          confirmColor: "#3CC51F",
                          success: function (e) {
                            e.confirm ? (m = !0) : e.cancel;
                          },
                          fail: function (e) {
                            console.log(e);
                          },
                        });
                        break;
                      case "button_function":
                        e.showActionSheet({
                          title: "功能选择",
                          itemList: [
                            "设置轮胎直径",
                            "仪表使用实时车速",
                            "查询蓝牙版本",
                            "深度学习模式",
                          ],
                          success: function (t) {
                            switch (t.tapIndex) {
                              case 3:
                                g = !0;
                                break;
                              case 0:
                                e.showModal({
                                  title: "轮胎直径(cm)",
                                  editable: !0,
                                  type: "number",
                                  placeholderText: n.diameter.toString(),
                                  success: function (t) {
                                    var a = t.content;
                                    /^[0-9]{1,4}$/.test(a) &&
                                      parseInt(a) <= 9999 &&
                                      ((n.diameter = parseInt(a)),
                                      e.setStorage({
                                        key: "wheel_l",
                                        data: n.diameter,
                                        success: function () {
                                          console.log("数据存储成功");
                                        },
                                        fail: function (e) {
                                          console.log("数据存储失败", e);
                                        },
                                        complete: function () {
                                          console.log("存储操作完成");
                                        },
                                      }));
                                  },
                                });
                                break;
                              case 1:
                                e.showToast({
                                  title: "敬请期待",
                                  icon: "exception",
                                  duration: 1e3,
                                });
                                break;
                              case 2:
                                if (n.ble_connect) {
                                  var a = "当前蓝牙支持升级!";
                                  0 == n.ble_isnew &&
                                    (a = "当前蓝牙不支持升级!"),
                                    e.showToast({
                                      title: a,
                                      icon: "success",
                                      duration: 2e3,
                                    });
                                } else
                                  e.showToast({
                                    title: "请先连接蓝牙",
                                    icon: "error",
                                    duration: 2e3,
                                  });
                            }
                          },
                          fail: function (e) {},
                        });
                        break;
                      case "button_error":
                        e.showModal({
                          title: "页面跳转提醒",
                          content:
                            "确认要跳转到常见问题页面吗?!可以直接点击显示的故障跳转到解析页",
                          cancelText: "我很忙的",
                          cancelColor: "#000000",
                          showCancel: !0,
                          confirmText: "送朕过去",
                          confirmColor: "#3CC51F",
                          success: function (t) {
                            t.confirm
                              ? e.navigateTo({
                                  url: "/subpackages/info/pages/faq/faq",
                                })
                              : t.cancel;
                          },
                          fail: function (e) {
                            console.log(e);
                          },
                        });
                        break;
                      case "button_check":
                        if (
                          this.single_soft_ver < 19 ||
                          this.single_soft_ver > 999
                        )
                          return void e.showToast({
                            title: "当前版本不支持蓝牙密码",
                            icon: "exception",
                            duration: 1500,
                          });
                        if (
                          1 == this.check_ble_status(!0, !1) ||
                          2 == this.check_ble_status(!0, !1)
                        )
                          return;
                        e.showModal({
                          title: "密码/Password",
                          editable: !0,
                          type: "number",
                          placeholderText: "0-9999",
                          success: function (t) {
                            var a = t.content.trim();
                            a.length > 4
                              ? e.showToast({
                                  title: "密码长度不能超过4位",
                                  icon: "none",
                                })
                              : /^[0-9]{1,4}$/.test(a) &&
                                parseInt(a) <= 9999 &&
                                ((v.user = parseInt(a)),
                                console.log("usr key" + v.user),
                                (b = !0));
                          },
                        });
                        break;
                      case "button_switch":
                        0 == this.Amplify
                          ? (this.Amplify = !0)
                          : (this.Amplify = !1);
                        break;
                      case "button_select":
                        this.initBLE();
                    }
                  },
                  buttonClickHandler: function (t, a) {
                    console.log(
                      "点击了按钮 ".concat(t, "，所属组的索引为 ").concat(a)
                    );
                    switch (a) {
                      case 0:
                        "button_0_1" === t
                          ? ((this.parameter_setting[0][0].value = 45),
                            (this.parameter_setting[0][1].value = 200),
                            (this.parameter_setting[1][7].value = 45),
                            (this.parameter_setting[5][1].value = 150))
                          : "button_0_2" === t &&
                            e.showModal({
                              title: "参考方法",
                              content:
                                "满把起步一般是母线电流大于保护板的保护阈值导致，尝试增加前进挡软启时间，或者减小母线电流至保护板阈值之下",
                              showCancel: !1,
                            });
                        break;
                      case 1:
                        switch (t) {
                          case "button_1_1":
                          case "button_1_2":
                            var n = this.buttonDataMap[t];
                            n
                              ? ((this.currentTableData = n.data),
                                (this.currentTableHeaders = n.headers))
                              : ((this.currentTableData = []),
                                (this.currentTableHeaders = [])),
                              (this.isTableModalShow = !0);
                            break;
                          case "button_1_3":
                            e.showModal({
                              title: "参考方法",
                              content:
                                "弱磁只有在高速挡有效，最大弱磁测试方法：后轮悬空状态下，用3挡测试，一边加弱磁，一边观察电机的转速和转动情况，如果转速不再提升或者电机在极速时有明显抖动和异音，或者松掉转把，电机没有明显降速，则说明弱磁达到极限。落地的极速有可能小于空转下的极速，属于正常现象。正常弱磁小于测试的极限即可，如果用不了那么大的转速，可以适当降低弱磁。",
                              showCancel: !1,
                            });
                            break;
                          case "button_1_4":
                            e.showModal({
                              title: "提示",
                              content: "正常的弱磁般为设置相线的1/3。",
                              showCancel: !1,
                            });
                            break;
                          case "button_1_5":
                            e.showModal({
                              title: "自学习常见问题",
                              content:
                                "1.提示自学习电流偏大--调大学习电流，可以10A的步进增加(记得写入参数) 2.提示自学习电流偏小或搜索超时--调小学习电流，可以10A的步进减少(记得写入参数)3.学习电流设置到10依然报超时或偏小--可能是缺项导致的，更换电机或控制器，两者都有可能导致这个问题。4.霍尔故障--更换电机的霍尔传感器 5.自学习成功后，控制器会滴一声，以这个来判断是否学习成功！",
                              showCancel: !1,
                            });
                        }
                        break;
                      case 2:
                        switch (t) {
                          case "button_2_1":
                            (this.parameter_setting[2][2].value = 0.3),
                              (this.parameter_setting[2][3].value = 2);
                            break;
                          case "button_2_2":
                            (this.parameter_setting[2][2].value = 5),
                              (this.parameter_setting[2][3].value = 1);
                            break;
                          case "button_2_3":
                            e.showModal({
                              title: "温馨提示",
                              content: "确认要跳转到转把适配教程吗",
                              showCancel: !0,
                              confirmText: "好的",
                              cancelText: "取消",
                              success: function (t) {
                                if (t.confirm) {
                                  "https://mp.weixin.qq.com/s/Y3ZbzJMo1H8XzdRKHIoulA",
                                    e.navigateTo({
                                      url: "/pages/webview/webview?url=".concat(
                                        encodeURIComponent(
                                          "https://mp.weixin.qq.com/s/Y3ZbzJMo1H8XzdRKHIoulA"
                                        )
                                      ),
                                    });
                                } else t.cancel && console.log("用户点击取消");
                              },
                            });
                            break;
                          case "button_2_4":
                            e.showModal({
                              title: "常见问题",
                              content:
                                "1.松掉转把时电机依然自动转:调大空转把电压。2.满把报转把故障或飞车保护:调大满转把电压。强烈建议根据要求手动适配转把！",
                              showCancel: !1,
                              confirmText: "好的",
                            });
                        }
                        break;
                      case 3:
                        switch (t) {
                          case "button_3_1":
                            e.showModal({
                              title: "TCS提示",
                              content:
                                "请确保完成了自学习！！TCS触发阈值设置为0表示关闭TCS功能，数值越小，触发越灵敏，太小容易误触发。TCS恢复时间用来调整退出TCS的平滑度，越大恢复时间越久，大家根据自己的体验来调整",
                              showCancel: !1,
                            });
                            break;
                          case "button_3_2":
                            e.showModal({
                              title: "长按巡航切倒挡",
                              content:
                                "第一步开启长按巡航切倒挡。第二步不松开巡航键的情况下，轻轻拧转把即可实现倒挡功能。",
                              showCancel: !1,
                            });
                            break;
                          case "button_3_3":
                            e.showModal({
                              title: "预警模式说明",
                              content:
                                "设置为无效：三速切换正常生效。设置为开启：强制三速均为低速挡设置。设置为关闭：三速切换依然正常，但是可以通过按住巡航键，同时操作刹车1长3短，即可设置为开启，此后三速只有低速挡生效。",
                              showCancel: !1,
                            });
                        }
                        break;
                      case 5:
                        switch (t) {
                          case "button_5_1":
                            e.showModal({
                              title: "表显车速与实际有偏差",
                              content:
                                "记录仪表显示车速 A 和实际车速 B，计算 A/B 得出系数，将该系数与当前一线通系数相乘，把结果写入控制器即可",
                              showCancel: !1,
                            });
                            break;
                          case "button_5_2":
                            e.showModal({
                              title: "里程不准",
                              content:
                                "里程不准，多数是由于表显车速不准导致，第一步先通过调整1挡的转速让实际车速控制在20以内。第二步调整控制器的一线通系数让表显车速和实际车速接近即可。",
                              showCancel: !1,
                            });
                            break;
                          case "button_5_3":
                            e.showModal({
                              title: "部分车型特殊处理",
                              content: "点击确认跳转到和和车型的相关说明介绍",
                              showCancel: !0,
                              success: function (t) {
                                t.confirm
                                  ? e.navigateTo({
                                      url: "/subpackages/info/pages/faqlist/faqlist?categoryIndex=4",
                                    })
                                  : t.cancel && console.log("用户点击取消");
                              },
                            });
                        }
                        break;
                      case 6:
                        switch (t) {
                          case "button_6_1":
                            e.showModal({
                              title: "换挡器接线说明",
                              content:
                                "先确认换挡器的线是否接到控制器(多数协议控接到仪表)，再确认你的换挡器类型，点动换挡器需要将换挡器的信号接到控制器的低速信号线上，加减挡和拨动换挡器要将换挡器的两根线分别接到控制器的低速和高速信号线，信号线为低电平有效。",
                              showCancel: !1,
                            });
                            break;
                          case "button_6_2":
                            e.showModal({
                              title: "无换挡器说明",
                              content:
                                "如果无换挡器又想使用高速挡设置如下，换挡方式选点动，点动默认挡位选高速挡，之后写入参数即可。",
                              showCancel: !1,
                            });
                        }
                        break;
                      case 8:
                        switch (t) {
                          case "button_8_1":
                            (this.parameter_setting[8][0].value = 34),
                              (this.parameter_setting[8][1].value = 95),
                              (this.parameter_setting[8][2].value = 42),
                              (this.parameter_setting[8][3].value = 35);
                            break;
                          case "button_8_2":
                            (this.parameter_setting[8][0].value = 44),
                              (this.parameter_setting[8][1].value = 95),
                              (this.parameter_setting[8][2].value = 52),
                              (this.parameter_setting[8][3].value = 45);
                            break;
                          case "button_8_3":
                            (this.parameter_setting[8][0].value = 50),
                              (this.parameter_setting[8][1].value = 95),
                              (this.parameter_setting[8][2].value = 56),
                              (this.parameter_setting[8][3].value = 51);
                            break;
                          case "button_8_4":
                            (this.parameter_setting[8][0].value = 57),
                              (this.parameter_setting[8][1].value = 95),
                              (this.parameter_setting[8][2].value = 65),
                              (this.parameter_setting[8][3].value = 58);
                            break;
                          case "button_8_5":
                            (this.parameter_setting[8][0].value = 63),
                              (this.parameter_setting[8][1].value = 95),
                              (this.parameter_setting[8][2].value = 69),
                              (this.parameter_setting[8][3].value = 64);
                        }
                        break;
                      case 9:
                        switch (t) {
                          case "button_9_1":
                            e.showModal({
                              title: "提示",
                              content:
                                "1.电池电压:只对电量显示起作用，大部分车型使用整车BMS或仪表计算电量，和控制器无关。可通过设置不同的电压测试电量是否变化，从而确定是否和控制器相关。 2.放电系数:不影响实际放电,只用作调整电量显示下降快慢，由于电池差异较大，且受环境影响较大，实际使用中如果发现电量显示下降明显快于实际放电，可适当在64基础上调小，反之亦然，另外该值和放电倍率并无直接关系，但一般来说，放电倍率标称高的电池放电能力强，可适当调小该参数，反之亦然。 3.电池串数：单节电芯额定电压三元锂电3.65V，磷酸铁锂3.2V，铅酸12V，例如48V三元锂电一般是13串",
                              showCancel: !1,
                            });
                        }
                        break;
                      case 10:
                        switch (t) {
                          case "button_10_1":
                            (this.parameter_setting[10][0].value = 5),
                              (this.parameter_setting[10][1].value = 5),
                              (this.parameter_setting[10][2].value = 10),
                              (this.parameter_setting[10][3].value = 100),
                              (this.parameter_setting[10][4].value = 90);
                            break;
                          case "button_10_2":
                            (this.parameter_setting[10][0].value = 10),
                              (this.parameter_setting[10][1].value = 10),
                              (this.parameter_setting[10][2].value = 20),
                              (this.parameter_setting[10][3].value = 100),
                              (this.parameter_setting[10][4].value = 90);
                            break;
                          case "button_10_3":
                            (this.parameter_setting[10][0].value = 0),
                              (this.parameter_setting[10][1].value = 0),
                              (this.parameter_setting[10][2].value = 0),
                              (this.parameter_setting[10][3].value = 3e3),
                              (this.parameter_setting[10][4].value = 0);
                        }
                        break;
                      case 11:
                        e.showModal({
                          title: "S_Ki",
                          content:
                            "强制锁电机，自动驻车开启后，由于不同电机不同车型差异，在强制锁电机和自动驻坡中如果抖动剧烈，可适当减小s_ki值，如果感觉溜坡距离太长，可适当加大s_ki值",
                          showCancel: !1,
                        });
                    }
                    1 !== a &&
                      e.showToast({
                        title: "记得写入数据",
                        icon: "exception",
                        duration: 1500,
                      });
                  },
                  parameter_text_handle: function (t, a, n) {
                    10 == a &&
                      5 == n &&
                      e.showModal({
                        title: "提示",
                        content:
                          "陡坡缓降需要在Ebs里设置最大充电电流，因为陡坡缓降会有充电电流，所以陡坡缓降力道会受该参数影响，另外，为了可单独关闭该功能，最大充电电流设置为偶数即可关闭，比如你设置10就是关的，设置9、11等都是开的",
                        showCancel: !1,
                        confirmText: "明白了解",
                        success: function (e) {
                          e.confirm || e.cancel;
                        },
                      });
                  },
                  parameter_click_handle: function (t, a, n) {
                    var r = this;
                    e.showModal({
                      title: t.name,
                      editable: !0,
                      placeholderText: t.value.toString(),
                      type: "number",
                      success: function (i) {
                        if (i.confirm) {
                          var o = i.content,
                            s = !1,
                            c = t.max,
                            l = t.min;
                          if (
                            ("uint" === t.type
                              ? (s = /^\d+$/.test(o))
                              : "int" === t.type
                              ? (s = /^-?\d+$/.test(o))
                              : "float" === t.type &&
                                (s = /^-?\d+(\.\d+)?$/.test(o)),
                            s)
                          ) {
                            var u = Number(o);
                            u > c || u < l
                              ? e.showToast({
                                  title: "范围:" + l + "--" + c,
                                  icon: "error",
                                  duration: 2e3,
                                })
                              : (r.$set(r.parameter_setting[a][n], "value", u),
                                console.log("用户输入有效，更新参数值为:", u),
                                10 == a &&
                                  2 == n &&
                                  r.$set(
                                    r.parameter_setting[10][5],
                                    "value",
                                    u
                                  ));
                          } else
                            e.showToast({
                              title: "输入格式不正确，请检查后重新输入",
                              icon: "none",
                            });
                        } else i.cancel;
                      },
                    });
                  },
                  parameter_switch_tip_handle: function (t, a, n) {
                    3 == a &&
                      1 == t.en &&
                      0 == C &&
                      e.showModal({
                        title: "警告",
                        content:
                          "检测到你没有完成自学习，请先完成自学习，再设置该功能,否则会很危险！！！",
                        showCancel: !1,
                        confirmText: "去自学习",
                        success: function (e) {
                          e.confirm || e.cancel;
                        },
                      });
                  },
                  param_picker_handle: function (e, t, a) {
                    var n = e.detail.value;
                    this.$set(this.parameter_setting[t][a], "value", n);
                  },
                  parameter_switch_handle: function (t, a, n) {
                    var r = t.detail.value;
                    if (
                      (console.log(t),
                      t.preventDefault(),
                      3 == a &&
                        4 == n &&
                        1 == r &&
                        e.showModal({
                          title: "温馨提示",
                          content: "刹车不断电请谨慎开启，强烈建议关闭！",
                          showCancel: !1,
                          confirmText: "明白了解",
                          success: function (e) {
                            e.confirm || e.cancel;
                          },
                        }),
                      3 == a && 1 == r && 0 == C)
                    )
                      return (
                        t.preventDefault(),
                        e.showModal({
                          title: "警告",
                          content:
                            "检测到你没有完成自学习，请先完成自学习，再设置该功能,否则会很危险！！！",
                          showCancel: !1,
                          confirmText: "去自学习",
                          success: function (e) {
                            e.confirm || e.cancel;
                          },
                        }),
                        void (t.detail.value = !1)
                      );
                    this.parameter_setting[a][n].reverse
                      ? this.$set(this.parameter_setting[a][n], "value", !r)
                      : this.$set(this.parameter_setting[a][n], "value", r);
                  },
                  parameter_list_handle: function (t, a, n) {
                    var r = this;
                    if (
                      (console.log("index:" + a + "paramIndex:" + n),
                      5 == a &&
                        0 == n &&
                        3 == this.realtime_page_data.serial_code)
                    ) {
                      var i = this.buttonDataMap.ae_car_sel;
                      (this.currentTableData = i.data),
                        (this.currentTableHeaders = i.headers),
                        (this.isTableModalShow = !0);
                    }
                    e.showActionSheet({
                      title: this.parameter_setting[a][n].name,
                      itemList: this.parameter_setting[a][n].description
                        ? this.parameter_setting[a][n].description
                        : this.parameter_setting[a][n].options,
                      success: function (e) {
                        var t = e.tapIndex;
                        r.$set(r.parameter_setting[a][n], "value", t),
                          (r.isTableModalShow = !1);
                      },
                      fail: function (e) {
                        console.log(e);
                      },
                    });
                  },
                  parameter_sheet_handle: function (t) {
                    var a = this,
                      n = this;
                    console.log(t),
                      e.showActionSheet({
                        title: t.currentTarget.dataset.buttonText,
                        itemList: n.parameter_sheet,
                        success: function (t) {
                          switch (t.tapIndex) {
                            case 0:
                              if (1 == n.check_ble_status(!1, !1)) return;
                              n.writeData("AA110001");
                              break;
                            case 1:
                              if (
                                1 == n.check_ble_status(!1, !0) ||
                                3 == n.check_ble_status(!1, !0)
                              )
                                return;
                              var r = "";
                              if (
                                (n.parameter_setting[8][3].value >
                                  n.parameter_setting[8][2].value &&
                                  (r += "检测到降载起始电压小于降载截至电压；"),
                                n.parameter_setting[8][0].value >
                                  n.parameter_setting[8][3].value &&
                                  (r += "检测到欠压点大于降载截至电压；"),
                                n.parameter_setting[8][1].value <
                                  n.parameter_setting[8][2].value &&
                                  (r += "检测到过压点设置有误；"),
                                n.parameter_setting[8][1].value <
                                  n.parameter_setting[10][4].value &&
                                  (r += "检测到EBS电压上限高于过压点；"),
                                n.parameter_setting[2][0].value <
                                  n.parameter_setting[2][1].value &&
                                  (r += "检测转把满把电压小于空转把电压；"),
                                n.parameter_setting[3][8].value > 0 &&
                                  n.parameter_setting[3][8].value < 10 &&
                                  (r +=
                                    "检测到Tcs的灵敏度可能过高导致误触发；"),
                                "" != r)
                              )
                                return void e.showModal({
                                  title: "参数异常提醒",
                                  content: r,
                                  cancelText: "取消操作",
                                  confirmText: "继续使用",
                                  success: function (e) {
                                    e.confirm
                                      ? n.writeData("AA210001")
                                      : e.cancel && console.log("用户点击取消");
                                  },
                                });
                              n.single_soft_ver < 22
                                ? e.showModal({
                                    title: "操作提示",
                                    content:
                                      "当前固件版本较低，建议使用老版本的调试软件",
                                    cancelText: "停止使用",
                                    confirmText: "继续使用",
                                    success: function (e) {
                                      e.confirm
                                        ? n.writeData("AA210001")
                                        : e.cancel &&
                                          console.log("用户点击取消");
                                    },
                                  })
                                : n.writeData("AA210001");
                              break;
                            case 3:
                              if (
                                1 == n.check_ble_status(!1, !0) ||
                                3 == n.check_ble_status(!1, !0)
                              )
                                return;
                              e.showModal({
                                title: "重要提醒",
                                content:
                                  "恢复出厂设置后,需要重新自学习和调参，蓝牙密码也会恢复成默认8888，确认要恢复出厂设置吗?",
                                cancelText: "取消",
                                confirmText: "确认",
                                success: function (e) {
                                  e.confirm
                                    ? n.writeData("AA210101")
                                    : e.cancel && console.log("用户点击取消");
                                },
                              });
                              break;
                            case 2:
                              e.showActionSheet({
                                title: "辅助调参功能‌",
                                itemList: n.realtime_page_data.identity,
                                success: function (t) {
                                  var a = t.tapIndex;
                                  if (0 != a || n.realtime_page_data.isNew)
                                    1 == a && n.realtime_page_data.isNew
                                      ? ((n.realtime_page_data.isNew = !1),
                                        n.parameter_title.splice(0),
                                        (n.parameter_title = (0,
                                        c.getparameter_title)()))
                                      : 2 == a
                                      ? e.navigateTo({
                                          url: "/subpackages/info/flowchart/flowchart",
                                        })
                                      : 3 == a &&
                                        (1 == n.parameter_tip_enable
                                          ? e.showModal({
                                              title: "操作提示",
                                              content:
                                                "确认要关闭调参提示吗?关闭后将不再显示各个参数组下面的提示按钮，新手还是建议大家打开的。",
                                              showCancel: !0,
                                              cancelText: "开启提示",
                                              cancelColor: "#000000",
                                              confirmText: "关闭提示",
                                              confirmColor: "#3c76ff",
                                              success: function (t) {
                                                t.confirm
                                                  ? ((n.parameter_tip_enable =
                                                      !1),
                                                    e.setStorageSync(
                                                      "parameter_tip_enable",
                                                      n.parameter_tip_enable
                                                    ))
                                                  : t.cancel &&
                                                    console.log("用户点击取消");
                                              },
                                            })
                                          : e.showModal({
                                              title: "操作提示",
                                              content:
                                                "确认要打开调参辅助提示吗？利器在手，我亦是大神级别，何惧调参之忧",
                                              showCancel: !0,
                                              cancelText: "关闭提示",
                                              cancelColor: "#000000",
                                              confirmText: "开启提示",
                                              confirmColor: "#3c76ff",
                                              success: function (t) {
                                                t.confirm
                                                  ? ((n.parameter_tip_enable =
                                                      !0),
                                                    e.setStorageSync(
                                                      "parameter_tip_enable",
                                                      n.parameter_tip_enable
                                                    ))
                                                  : t.cancel &&
                                                    console.log("用户点击取消");
                                              },
                                            }));
                                  else {
                                    (n.realtime_page_data.isNew = !0),
                                      n.parameter_title.splice(0);
                                    for (
                                      var r = (0, c.getparameter_title)(),
                                        i = 0;
                                      i < f.length;
                                      i++
                                    )
                                      n.parameter_title.push(r[f[i].index]),
                                        (n.parameter_title[i].isarrow =
                                          f[i].arrow);
                                  }
                                },
                                fail: function (e) {},
                              });
                              break;
                            case 4:
                              if (
                                1 == n.check_ble_status(!1, !0) ||
                                3 == n.check_ble_status(!1, !0)
                              )
                                return;
                              if (
                                a.single_soft_ver < 19 ||
                                a.single_soft_ver > 999
                              )
                                return void e.showToast({
                                  title: "当前版本不支持蓝牙密码",
                                  icon: "exception",
                                  duration: 1500,
                                });
                              e.showModal({
                                title: "修改蓝牙密码",
                                editable: !0,
                                placeholderText:
                                  n.parameter_read[60].toString(),
                                type: "number",
                                success: function (t) {
                                  if (t.confirm) {
                                    var a = t.content;
                                    if (t.content.trim().length > 4)
                                      return void e.showToast({
                                        title: "密码长度不能超过4位",
                                        icon: "none",
                                      });
                                    if (/^\d+$/.test(a)) {
                                      var r = Number(a);
                                      r > 9999 || r < 0
                                        ? e.showToast({
                                            title: "输入范围:0--9999",
                                            icon: "error",
                                            duration: 2e3,
                                          })
                                        : ((n.parameter_read[60] = r),
                                          (n.realtime_page_data.isLock = !1),
                                          (0, _.savePasswordRecord)(r),
                                          (v.user = r),
                                          e.setStorageSync(
                                            "blePassword",
                                            v.user
                                          ),
                                          n.writeData("AA210001"));
                                    } else
                                      e.showToast({
                                        title:
                                          "输入格式不正确，请检查后重新输入",
                                        icon: "none",
                                      });
                                  } else t.cancel;
                                },
                              });
                              break;
                            case 5:
                              n.Switch_battery();
                          }
                        },
                        fail: function (e) {},
                      });
                  },
                  Switch_battery: function () {
                    e.$on("batteryParamsUpdated", this.handleBatteryData),
                      e.navigateTo({
                        url: "/subpackages/info/battery/battery",
                      });
                  },
                  handleBatteryData: function (t) {
                    console.log("接收到电池参数：", t);
                    t.batteryIndex;
                    var a = t.params;
                    (this.parameter_setting[0][0].value = Number(a.busCurrent)),
                      (this.parameter_setting[0][1].value = Number(
                        a.phaseCurrent
                      )),
                      (this.parameter_setting[1][7].value = Number(
                        a.weakMagnetCurrent
                      )),
                      (this.parameter_setting[8][0].value = Number(
                        a.underVoltagePoint
                      )),
                      (this.parameter_setting[8][1].value = Number(
                        a.overVoltagePoint
                      )),
                      (this.parameter_setting[8][2].value = Number(
                        a.loadReductionStartVoltage
                      )),
                      (this.parameter_setting[8][3].value = Number(
                        a.loadReductionEndVoltage
                      )),
                      e.showToast({
                        title: "记得写入数据",
                        icon: "exception",
                        duration: 1500,
                      });
                  },
                  onUpgradeClicked: function () {
                    var t = this;
                    if (0 == $) {
                      if (
                        1 != this.check_ble_status(!1, !0) &&
                        3 != this.check_ble_status(!1, !0)
                      )
                        if (t.ble_isnew) {
                          if (1 == $)
                            return (
                              t.writeData2("0103"),
                              void e.showToast({
                                title: "固件可能丢失",
                                icon: "error",
                                duration: 2e3,
                              })
                            );
                          if ((0 == F && (t.inputKey = ""), "" != t.inputKey)) {
                            if (
                              "&" !== t.inputKey.charAt(t.inputKey.length - 2)
                            )
                              return void e.showToast({
                                title: "指令字段错误",
                                icon: "error",
                                duration: 2e3,
                              });
                            var a = t.inputKey.slice(-1);
                            if (
                              ((t.manufacturerFirmwareId = Number(a)),
                              0 != t.manufacturerFirmwareId &&
                                2 != t.manufacturerFirmwareId &&
                                3 != t.manufacturerFirmwareId &&
                                6 != t.manufacturerFirmwareId)
                            )
                              return void e.showToast({
                                title: "指令错误",
                                icon: "error",
                                duration: 2e3,
                              });
                            var n = t.inputKey.slice(0, -2);
                            this.decrypt(n)
                              .then(function (a) {
                                a
                                  ? a.num1 === t.hard_ver
                                    ? ((t.hard_ver = a.num2),
                                      console.log("that.hard_ver" + t.hard_ver),
                                      e.showModal({
                                        title: "提醒",
                                        content:
                                          "升级前，请先查看升级教程和版本差异，确认是否需要升级。升级过程中尽量不要操作手机，并尽可能让手机靠近控制器。",
                                        success: function (e) {
                                          e.confirm &&
                                            (console.log("开始写数据"),
                                            (y = 4),
                                            t.writeData("AA16004C58B3A7"),
                                            setTimeout(function () {
                                              t.writeData2("0103");
                                            }, 300));
                                        },
                                      }))
                                    : e.showToast({
                                        title: "原控异常",
                                        icon: "exception",
                                        duration: 2e3,
                                      })
                                  : console.log("解密执行完毕，但未成功解密");
                              })
                              .catch(function (e) {
                                console.error("解密过程中出现异常:", e);
                              });
                          } else {
                            if (
                              1 !=
                              (0, d.canUpgrade)(
                                this.currentFirmwareVersion,
                                this.targetFirmwareVersion
                              )
                            )
                              return void e.showToast({
                                title: "暂无更新",
                                icon: "exception",
                                duration: 2e3,
                              });
                            e.showModal({
                              title: "提醒",
                              content:
                                "升级前，请先查看升级教程和版本差异，确认是否需要升级。升级过程中尽量不要操作手机，并尽可能让手机靠近控制器。",
                              success: function (e) {
                                e.confirm &&
                                  (console.log("开始写数据"),
                                  (y = 4),
                                  t.writeData("AA16004C58B3A7"),
                                  setTimeout(function () {
                                    t.writeData2("0103");
                                  }, 300));
                              },
                            });
                          }
                        } else
                          e.showToast({
                            title: "蓝牙模块不支持升级",
                            icon: "exception",
                            duration: 2e3,
                          });
                    } else
                      e.showToast({
                        title: "尝试恢复中",
                        icon: "exception",
                        duration: 2e3,
                      });
                  },
                  getfirmwaveinfoClicked: function () {
                    e.navigateTo({
                      url: "../../subpackages/info/pages/firmwave/firmwave?code=".concat(
                        this.realtime_page_data.serial_code
                      ),
                    });
                  },
                  initBLE: function () {
                    var t = this;
                    return (0, o.default)(
                      i.default.mark(function a() {
                        var n;
                        return i.default.wrap(function (a) {
                          for (;;)
                            switch ((a.prev = a.next)) {
                              case 0:
                                if (((n = t), !t.ble_connect)) {
                                  a.next = 5;
                                  break;
                                }
                                return (a.next = 4), t.stopConnection();
                              case 4:
                                t.ble_connect = !1;
                              case 5:
                                if (!n.isConnecting) {
                                  a.next = 7;
                                  break;
                                }
                                return a.abrupt("return");
                              case 7:
                                (n.isConnecting = !0),
                                  u.default
                                    .getBluetoothState()
                                    .then(function () {
                                      u.default
                                        .discoveryBluetooth()
                                        .then(function () {
                                          u.default
                                            .getBluetoothDevices()
                                            .then(function (a) {
                                              a
                                                ? u.default
                                                    .connectBluetooth()
                                                    .then(function () {
                                                      (n.ble_connect = !0),
                                                        (n.isConnecting = !1);
                                                      var t =
                                                        u.default.getConnectedDevice()
                                                          .name;
                                                      "GEKOO-BLE" ===
                                                      t.substring(0, 9)
                                                        ? ((n.ble_isnew = !0),
                                                          setTimeout(
                                                            function () {
                                                              B >= 40 &&
                                                                (($ = !0),
                                                                (n.read_flag =
                                                                  !0),
                                                                e.showModal({
                                                                  title: "提醒",
                                                                  content:
                                                                    "检测到固件丢失，请选择是否尝试恢复固件",
                                                                  cancelText:
                                                                    "返厂处理",
                                                                  confirmText:
                                                                    "尝试恢复",
                                                                  success:
                                                                    function (
                                                                      e
                                                                    ) {
                                                                      e.confirm &&
                                                                        ((n.TabCur = 2),
                                                                        n.writeData2(
                                                                          "0103"
                                                                        ),
                                                                        setTimeout(
                                                                          function () {
                                                                            n.writeData2(
                                                                              "0103"
                                                                            );
                                                                          },
                                                                          500
                                                                        ));
                                                                    },
                                                                  fail: function (
                                                                    e
                                                                  ) {
                                                                    console.log(
                                                                      e
                                                                    );
                                                                  },
                                                                }));
                                                            },
                                                            5e3
                                                          ))
                                                        : ((n.ble_isnew = !1),
                                                          (t = "GEKOOBLE")),
                                                        u.default.setDisconnectionCallback(
                                                          function () {
                                                            (n.read_flag = !1),
                                                              (T = !1),
                                                              n.stop_realtime_update(),
                                                              1 ==
                                                                n.ble_connect &&
                                                                ((n.ble_connect =
                                                                  !1),
                                                                e.showModal({
                                                                  title: n.$t(
                                                                    "ble_msg.discon.title"
                                                                  ),
                                                                  content: n.$t(
                                                                    "ble_msg.discon.content"
                                                                  ),
                                                                  showCancel:
                                                                    !1,
                                                                }));
                                                          }
                                                        ),
                                                        u.default.setDataCallback(
                                                          function (e) {
                                                            n.bluetooth_data_handle(
                                                              e
                                                            );
                                                          }
                                                        ),
                                                        (n.TabCur = 0);
                                                    })
                                                    .catch(function () {
                                                      (n.ble_connect = !1),
                                                        (n.isConnecting = !1),
                                                        e.showModal({
                                                          title:
                                                            t.$t(
                                                              "ble_msg.conn.title"
                                                            ),
                                                          content: t.$t(
                                                            "ble_msg.conn.content"
                                                          ),
                                                          showCancel: !1,
                                                        });
                                                    })
                                                : ((n.ble_connect = !1),
                                                  (n.isConnecting = !1),
                                                  e.showModal({
                                                    title: t.$t(
                                                      "ble_msg.search.title"
                                                    ),
                                                    content: t.$t(
                                                      "ble_msg.search.content1"
                                                    ),
                                                    showCancel: !1,
                                                  }));
                                            })
                                            .catch(function () {
                                              (n.ble_connect = !1),
                                                (n.isConnecting = !1),
                                                e.showModal({
                                                  title: t.$t(
                                                    "ble_msg.search.title"
                                                  ),
                                                  content: t.$t(
                                                    "ble_msg.search.content"
                                                  ),
                                                  showCancel: !1,
                                                });
                                            });
                                        });
                                    })
                                    .catch(function (a) {
                                      (n.ble_connect = !1),
                                        (n.isConnecting = !1),
                                        (t.errorMsg = "连接失败");
                                      var r = "蓝牙不可用";
                                      switch (a) {
                                        case 103:
                                          r = t.$t("ble_msg.error.content1");
                                          break;
                                        case 10001:
                                          r = t.$t("ble_msg.error.content2");
                                      }
                                      e.showModal({
                                        title: t.$t("ble_msg.error.title"),
                                        content: r,
                                        showCancel: !1,
                                        confirmText: "确定",
                                        confirmColor: "#3CC51F",
                                        success: function (e) {
                                          e.confirm &&
                                            console.log("用户点击了确定");
                                        },
                                        fail: function (e) {
                                          console.log(e);
                                        },
                                      });
                                    });
                              case 9:
                              case "end":
                                return a.stop();
                            }
                        }, a);
                      })
                    )();
                  },
                  stopConnection: function () {
                    var e = this;
                    return (0, o.default)(
                      i.default.mark(function t() {
                        return i.default.wrap(function (t) {
                          for (;;)
                            switch ((t.prev = t.next)) {
                              case 0:
                                return (
                                  e.stop_realtime_update(),
                                  (t.next = 3),
                                  u.default.disconnectBluetooth()
                                );
                              case 3:
                              case "end":
                                return t.stop();
                            }
                        }, t);
                      })
                    )();
                  },
                  writeData: function (e) {
                    return (0, o.default)(
                      i.default.mark(function t() {
                        return i.default.wrap(function (t) {
                          for (;;)
                            switch ((t.prev = t.next)) {
                              case 0:
                                try {
                                  m
                                    ? (u.default.writeData(
                                        "AA1406000000000000"
                                      ),
                                      (m = !1))
                                    : g
                                    ? (u.default.writeData(
                                        "AA1406050000000000"
                                      ),
                                      (g = !1))
                                    : b
                                    ? (u.default.writeData("AA170001"),
                                      (b = !1))
                                    : u.default.writeData(e);
                                } catch (e) {
                                  console.error("写入数据时出错", e);
                                }
                              case 1:
                              case "end":
                                return t.stop();
                            }
                        }, t);
                      })
                    )();
                  },
                  writeData2: function (e) {
                    return (0, o.default)(
                      i.default.mark(function t() {
                        return i.default.wrap(function (t) {
                          for (;;)
                            switch ((t.prev = t.next)) {
                              case 0:
                                try {
                                  u.default.writeData2(e);
                                } catch (e) {
                                  console.error("写入数据时出错", e);
                                }
                              case 1:
                              case "end":
                                return t.stop();
                            }
                        }, t);
                      })
                    )();
                  },
                  sendDataInPackets: function (e) {
                    var t = arguments,
                      a = this;
                    return (0, o.default)(
                      i.default.mark(function n() {
                        var r, o, s, c, l, _, d;
                        return i.default.wrap(function (n) {
                          for (;;)
                            switch ((n.prev = n.next)) {
                              case 0:
                                for (
                                  r =
                                    t.length > 1 && void 0 !== t[1] ? t[1] : 20,
                                    o = u.default.ab2hex(e),
                                    s = [],
                                    c = 0;
                                  c < o.length;
                                  c += 2 * r
                                )
                                  s.push(o.slice(c, c + 2 * r));
                                (l = i.default.mark(function e() {
                                  var t;
                                  return i.default.wrap(function (e) {
                                    for (;;)
                                      switch ((e.prev = e.next)) {
                                        case 0:
                                          return (
                                            (t = d[_]),
                                            (e.next = 3),
                                            new Promise(function (e) {
                                              a.writeData(t),
                                                setTimeout(e, 100);
                                            })
                                          );
                                        case 3:
                                        case "end":
                                          return e.stop();
                                      }
                                  }, e);
                                })),
                                  (_ = 0),
                                  (d = s);
                              case 6:
                                if (!(_ < d.length)) {
                                  n.next = 11;
                                  break;
                                }
                                return n.delegateYield(l(), "t0", 8);
                              case 8:
                                _++, (n.next = 6);
                                break;
                              case 11:
                              case "end":
                                return n.stop();
                            }
                        }, n);
                      })
                    )();
                  },
                  start_realtime_update: function () {
                    var e = this;
                    this.stop_realtime_update(),
                      console.log("开启定时刷新"),
                      (this.realtime_page_data.timer = setInterval(function () {
                        e.ble_connect && e.writeData("AA13FF01AA130001");
                      }, 100));
                  },
                  stop_realtime_update: function () {
                    this.realtime_page_data.timer &&
                      (clearInterval(this.realtime_page_data.timer),
                      (this.realtime_page_data.timer = null),
                      console.log("关闭定时刷新"));
                  },
                  bluetooth_data_handle: function (e) {
                    var t = this;
                    for (
                      this.dataBuffer = this.dataBuffer.concat(e),
                        this.ble_r_ok = !0,
                        0 == p &&
                          (setTimeout(function () {
                            b = !0;
                          }, 200),
                          (p = !0)),
                        this.timer && clearTimeout(this.timer),
                        this.timer = setTimeout(function () {
                          (t.dataBuffer = []),
                            (t.ble_r_ok = !1),
                            console.log("超时，清空缓存");
                        }, t.timeoutDuration);
                      this.dataBuffer.length > 0;

                    ) {
                      var a = this.dataBuffer.indexOf(170);
                      if (-1 === a) {
                        B++, (this.dataBuffer = []);
                        break;
                      }
                      if (
                        ((B = 0),
                        a > 0 && this.dataBuffer.splice(0, a),
                        this.dataBuffer.length < 2)
                      )
                        break;
                      var n = this.dataBuffer[1],
                        r = this.getFrameLength(n);
                      if (r)
                        if (
                          19 != n ||
                          0 != this.dataBuffer[2] ||
                          1 != this.dataBuffer[3]
                        ) {
                          if (this.dataBuffer.length < r) break;
                          var i = this.dataBuffer.slice(0, r);
                          this.parseProtocol(n, i),
                            this.dataBuffer.splice(0, r);
                        } else this.dataBuffer.splice(0, 4);
                      else this.dataBuffer.splice(0, 2);
                    }
                  },
                  getFrameLength: function (e) {
                    switch (e) {
                      case 17:
                        return 131;
                      case 19:
                        return 67;
                      case 22:
                        return console.log("cmd_prog_rev_count" + y), y;
                      case 23:
                        return 5;
                      case 18:
                      case 33:
                        return 4;
                      default:
                        return null;
                    }
                  },
                  parseProtocol: function (t, a) {
                    var n = this,
                      r = this,
                      i = 0,
                      o = [],
                      s = [];
                    switch (t) {
                      case 22:
                        if (22 == a[1] && 1 == a[2])
                          (y = 19), r.writeData("AA161601");
                        else if (22 == a[1] && 37 == a[2])
                          (S = 0),
                            r.writeData(A[S]),
                            (S = 1),
                            (r.prog_progess = Math.round((100 * S) / D)),
                            e.showLoading({ title: "正在升级固件", mask: !0 });
                        else if (22 == a[1] && 53 == a[2])
                          S >= D
                            ? (e.hideLoading(), r.writeData("AA164501"))
                            : (r.writeData(A[S]),
                              S++,
                              (r.prog_progess = Math.round((100 * S) / D)));
                        else if (22 == a[1] && 69 == a[2])
                          (r.prog_progess = 0),
                            e.hideLoading(),
                            1 == a[3]
                              ? (r.writeData2("0102"),
                                (r.TabCur = 0),
                                ($ = !1),
                                e.showToast({
                                  title: "烧录成功",
                                  icon: "success",
                                  duration: 1e3,
                                }))
                              : e.showToast({
                                  title: "烧录失败",
                                  icon: "error",
                                  duration: 1e3,
                                });
                        else if (22 == a[1] && 22 == a[2]) {
                          console.log(a);
                          var l = a[4];
                          (l <<= 8), (l += a[3]);
                          var h = a[6];
                          (h <<= 8),
                            (h += a[5]),
                            console.log("prog_boot_soft" + h);
                          var f = l.toString(16);
                          if (
                            ((M = parseInt(f, 10)),
                            console.log("prog_boot_hard" + M),
                            0 != r.hard_ver)
                          )
                            (M = r.hard_ver),
                              console.log("that.hard_ver" + r.hard_ver);
                          else {
                            var p = e.getStorageSync("hard_ver");
                            if ((console.log("save_hard_ver" + p), p))
                              if (p != M) p % 1e3 == M % 1e3 && (M = p);
                          }
                          var m = (0, d.getUpdateCode)(
                            M,
                            r.manufacturerFirmwareId
                          );
                          console.log("hex_info" + m.hex + "-" + m.index),
                            4 === m.hex
                              ? (A = P.get_hex_src(m.index))
                              : 3 === m.hex
                              ? (A = I.get_hex_src(m.index))
                              : 7 === m.hex
                              ? (A = L.get_hex_src(m.index))
                              : 0 === m.hex
                              ? (A = R.get_hex_src(m.index))
                              : 8 === m.hex
                              ? (A = V.get_hex_src(m.index))
                              : 6 === m.hex
                              ? (A = q.get_hex_src(m.index))
                              : 5 === m.hex && (A = N.get_hex_src(m.index)),
                            null == A ||
                              ((D = A.length),
                              (y = 4),
                              r.writeData("AA162501"));
                        }
                        break;
                      case 23:
                        var g = a[3],
                          b = a[4];
                        (b <<= 8),
                          (b += g),
                          (v.dev = b),
                          console.log("BLE KEY check" + v.user),
                          v.dev == v.user
                            ? ((this.realtime_page_data.isLock = !0),
                              e.setStorageSync("blePassword", v.user))
                            : (this.realtime_page_data.isLock = !1);
                        break;
                      case 17:
                        i = 0;
                        for (var x = 3; x < a.length; x += 2) {
                          var B = a[x],
                            F = (a[x + 1] << 8) | B;
                          s.push(F), (i += F), (i &= 65535);
                        }
                        if (65535 == (65535 & i)) {
                          (this.read_flag = !0),
                            console.log("参数读取正常"),
                            (this.parameter_setting = (0,
                            c.parameter_process_data)(s, this.single_soft_ver)),
                            (this.parameter_read = s),
                            (this.polePairs = s[34]);
                          var O = new Date(),
                            E = this.formatDate(O);
                          (this.key_time = E),
                            (v.dev = s[60]),
                            v.dev === v.user &&
                              (this.realtime_page_data.isLock = !0);
                          var H = E.split(":");
                          if (4 == H.length) {
                            var K;
                            K = H[0] + H[2] + H[1];
                            var U = Number(K);
                            if (1 == this.realtime_page_data.isLock)
                              (U += s[60]), (U += Number(H[3]));
                            else {
                              var z = Number(H[1]);
                              z >= 12 && (z -= 12);
                              var j = this.parameter_setting[11][z].value;
                              (U += s[60] + j), (U -= Number(H[3]));
                            }
                            this.key_crc = U;
                          }
                          e.showToast({
                            title: "参数读取成功",
                            icon: "success",
                          });
                          var G = 240 & s[61];
                          if (
                            (2 == (G >>= 4) && 400 == s[55] && 100 == s[56]) ||
                            (3 == G && 260 == s[55] && 60 == s[56]) ||
                            (4 == G && 200 == s[55] && 45 == s[56]) ||
                            (5 == G && 200 == s[55] && 45 == s[56]) ||
                            (7 == G && 201 == s[55] && 45 == s[56]) ||
                            (8 == G && 200 == s[55] && 45 == s[56])
                          )
                            C = !1;
                          else {
                            C = !0;
                            var J = this.parameter_setting[3];
                            Array.isArray(J) &&
                              J.length > 0 &&
                              J.forEach(function (e) {
                                "bool" === (null == e ? void 0 : e.type) &&
                                  (e.hasOwnProperty("en")
                                    ? (e.en = !1)
                                    : n.$set(e, "en", !1));
                              });
                          }
                        }
                        break;
                      case 33:
                        1 == a[2]
                          ? 170 == a[3]
                            ? (e.showToast({
                                title: "恢复出厂设置成功",
                                icon: "success",
                                duration: 1500,
                              }),
                              (v.user = 8888),
                              e.setStorageSync("blePassword", v.user),
                              setTimeout(function () {
                                r.writeData("AA110001");
                              }, 500))
                            : e.showToast({
                                title: "恢复出厂设置失败",
                                icon: "error",
                              })
                          : (console.log("控制器准备好写入数据"),
                            (this.parameter_write = (0, c.renderDataToOriginal)(
                              this.parameter_read,
                              this.parameter_setting
                            )),
                            this.sendDataInPackets(this.parameter_write));
                        break;
                      case 18:
                        if (170 == a[3]) {
                          e.showToast({
                            title: "参数修改成功",
                            icon: "success",
                          });
                          var Y = this;
                          setTimeout(function () {
                            Y.writeData("AA110001");
                          }, 500);
                        } else
                          e.showToast({ title: "参数修改失败", icon: "error" });
                        break;
                      case 19:
                        for (var X = 3; X < a.length; X += 2) {
                          var Z = a[X],
                            Q = (a[X + 1] << 8) | Z;
                          o.push(Q), (i += Q), (i &= 65535);
                        }
                        if (65535 == (65535 & i)) {
                          var W = (0, _.parseHardwareModel)(o[29]);
                          e.setStorageSync("hard_ver", o[29]),
                            (this.realtime_page_data.serial_code =
                              W.thousandsDigit),
                            (this.realtime_page_data.m_debug = W.hardwareModel),
                            (this.busCurrentValue = W.busCurrent),
                            (this.phaseCurrentValue = W.phaseCurrent),
                            (this.debug_buffer = o);
                          var ee = (0, _.processData)(o);
                          if (
                            ((this.realtime_page_data.display =
                              ee.processedData),
                            (this.realtime_page_data.status =
                              ee.learnself_status_str),
                            (this.realtime_page_data.func_status = ee.fun_str),
                            (this.realtime_page_data.gear_io_info =
                              ee.bitStatus),
                            (this.targetData = ee.func_bit),
                            (this.currentFirmwareVersion = ee.soft_ver),
                            (this.manufacturerFirmwareId = ee.manufacturer_id),
                            (this.hard_ver = ee.hard_ver),
                            (this.single_soft_ver = ee.firmware_ver),
                            this.single_soft_ver >= 25)
                          )
                            if (
                              "GEKOO-BLE" ===
                              u.default
                                .getConnectedDevice()
                                .name.substring(0, 9)
                            );
                            else {
                              var te = this.realtime_page_data.serial_code;
                              if (
                                4 != te &&
                                8 != te &&
                                6 != te &&
                                3 != te &&
                                7 != te
                              ) {
                                var ae = Math.floor(10 * Math.random());
                                if (0 == k && ((k = !0), ae <= 1))
                                  return void e.navigateBack({ delta: 1 });
                              }
                            }
                          0 == T &&
                            (this.targetFirmwareVersion = (0,
                            d.getFirmwareInfo)(o[29], ee.manufacturer_id)),
                            (w = this.realtime_page_data.display[2].value);
                          var ne = (0, _.calc_dashboard)(w, this.diameter);
                          if (
                            ((this.realtime_page_data.voltage_zoom =
                              this.realtime_page_data.display[0].value.toFixed(
                                0
                              )),
                            (this.realtime_page_data.speed_zoom =
                              ne.speed.toFixed(0)),
                            (this.realtime_page_data.speed = ne.s_f),
                            (this.realtime_page_data.velocity = ne.v_f),
                            this.speed >= 10 && 0 == this.calc_lock)
                          )
                            if (
                              (this.speed > this.speed_max &&
                                (this.speed_max = this.speed),
                              Math.abs(this.speed - this.speed_max) < 1)
                            ) {
                              if (
                                (this.speed_calc_cnt++,
                                this.speed_calc_cnt >= 5)
                              ) {
                                var re = (
                                  this.realtime_page_data.speed_zoom /
                                  this.speed
                                ).toFixed(2);
                                this.polePairs *= re;
                                var ie = this.polePairs;
                                (this.calc_lock = !0),
                                  e.showModal({
                                    title: "检测结束",
                                    content: "结果:" + ie.toString(),
                                  });
                              }
                            } else this.speed_calc_cnt = 0;
                        }
                    }
                  },
                  get_location_speed: function () {
                    var t = this;
                    e.startLocationUpdate({
                      success: function () {
                        e.onLocationChange(function (e) {
                          if (void 0 !== e.speed && null !== e.speed) {
                            console.log("当前速度：", e.speed);
                            var a = (3.6 * e.speed).toFixed(2);
                            (t.speed = a), !0;
                          } else console.log("未能获取到速度信息");
                        });
                      },
                      fail: function (e) {
                        console.error("启动位置更新失败:", e);
                      },
                    });
                  },
                  formatDate: function (e) {
                    var t = String(e.getDate()).padStart(2, "0"),
                      a = String(e.getHours()).padStart(2, "0"),
                      n = String(e.getMinutes()).padStart(2, "0"),
                      r = String(e.getSeconds()).padStart(2, "0");
                    return ""
                      .concat(t, ":")
                      .concat(a, ":")
                      .concat(n, ":")
                      .concat(r);
                  },
                },
                onLoad: function () {
                  var t = this;
                  e.setNavigationBarTitle({ title: this.$t("ble_page.name") }),
                    (p = !1);
                  var a = e.getStorageSync("blePassword");
                  a && (v.user = a),
                    console.log("ble_key.user" + v.user + "start_flag:" + p),
                    e.getStorage({
                      key: "wheel_l",
                      success: function (e) {
                        t.diameter = e.data;
                      },
                      fail: function (e) {
                        console.log("数据获取失败", e), (t.diameter = 46);
                      },
                      complete: function () {
                        console.log("获取操作完成");
                      },
                    });
                  var r = e.getStorageSync("parameter_tip_enable");
                  "" !== r && (this.parameter_tip_enable = r), this.initBLE();
                  var i = n.getAccountInfoSync().miniProgram.envVersion;
                  console.log("accountInfo" + i),
                    "trial" == i && ((F = !0), (this.debug_data = !0)),
                    require("../../subpackages/firmwave_ae/static/src.js", function (
                      e
                    ) {
                      I = e;
                    }, function (e) {
                      var t = e.errMsg,
                        a = e.mod;
                      console.error("path: ".concat(a, ", ").concat(t));
                    }),
                    require("../../subpackages/firmwave_cp/static/src.js", function (
                      e
                    ) {
                      L = e;
                    }, function (e) {
                      var t = e.errMsg,
                        a = e.mod;
                      console.error("path: ".concat(a, ", ").concat(t));
                    }),
                    require("../../subpackages/firmwave_gk/static/src.js", function (
                      e
                    ) {
                      R = e;
                    }, function (e) {
                      var t = e.errMsg,
                        a = e.mod;
                      console.error("path: ".concat(a, ", ").concat(t));
                    }),
                    require("../../subpackages/firmwave_nd/static/src.js", function (
                      e
                    ) {
                      V = e;
                    }, function (e) {
                      var t = e.errMsg,
                        a = e.mod;
                      console.error("path: ".concat(a, ", ").concat(t));
                    }),
                    require("../../subpackages/firmwave_pd/static/src.js", function (
                      e
                    ) {
                      q = e;
                    }, function (e) {
                      var t = e.errMsg,
                        a = e.mod;
                      console.error("path: ".concat(a, ", ").concat(t));
                    }),
                    require("../../subpackages/firmwave_ub/static/src.js", function (
                      e
                    ) {
                      N = e;
                    }, function (e) {
                      var t = e.errMsg,
                        a = e.mod;
                      console.error("path: ".concat(a, ", ").concat(t));
                    }),
                    require("../../subpackages/firmwave_xm/static/src.js", function (
                      e
                    ) {
                      P = e;
                    }, function (e) {
                      var t = e.errMsg,
                        a = e.mod;
                      console.error("path: ".concat(a, ", ").concat(t));
                    });
                },
                onUnload: function () {
                  e.stopLocationUpdate({
                    success: function () {
                      console.log("停止位置更新成功"), !1;
                    },
                    fail: function (e) {
                      console.error("停止位置更新失败:", e);
                    },
                  }),
                    this.stop_realtime_update(),
                    this.timer && clearTimeout(this.timer),
                    (this.timer = null),
                    e.closeBluetoothAdapter({ success: function (e) {} }),
                    (this.realtime_page_data.display = []);
                },
              };
            t.default = O;
          }).call(
            this,
            a(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
              .default,
            a(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1)
              .default
          );
        },
      71:
        /*!**************************************************************************************************************************************!*\
    !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/ble_debug/ble_debug.vue?vue&type=style&index=0&id=0f056466&lang=less&scoped=true& ***!
    \**************************************************************************************************************************************/
        /*! no static exports found */
        function (e, t, a) {
          a.r(t);
          var n = a(
              /*! -!./node_modules/mini-css-extract-plugin/dist/loader.js??ref--10-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--10-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--10-oneOf-1-2!./node_modules/postcss-loader/src??ref--10-oneOf-1-3!./node_modules/less-loader/dist/cjs.js??ref--10-oneOf-1-4!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--10-oneOf-1-5!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./ble_debug.vue?vue&type=style&index=0&id=0f056466&lang=less&scoped=true& */ 72
            ),
            r = a.n(n);
          for (var i in n)
            ["default"].indexOf(i) < 0 &&
              (function (e) {
                a.d(t, e, function () {
                  return n[e];
                });
              })(i);
          t.default = r.a;
        },
      72:
        /*!**********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
    !*** ./node_modules/mini-css-extract-plugin/dist/loader.js??ref--10-oneOf-1-0!./node_modules/css-loader/dist/cjs.js??ref--10-oneOf-1-1!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/stylePostLoader.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--10-oneOf-1-2!./node_modules/postcss-loader/src??ref--10-oneOf-1-3!./node_modules/less-loader/dist/cjs.js??ref--10-oneOf-1-4!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--10-oneOf-1-5!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages/ble_debug/ble_debug.vue?vue&type=style&index=0&id=0f056466&lang=less&scoped=true& ***!
    \**********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
        /*! no static exports found */
        function (e, t, a) {},
    },
    [[59, "common/runtime", "common/vendor"]],
  ]);

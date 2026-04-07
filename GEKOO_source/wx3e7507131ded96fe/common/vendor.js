require("../@babel/runtime/helpers/Arrayincludes");
var e = require("../@babel/runtime/helpers/typeof");
(global.webpackJsonp = global.webpackJsonp || []).push([
  ["common/vendor"],
  [
    ,
    /*!*********************************************************!*\
  !*** ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js ***!
  \*********************************************************/
    /*! no static exports found */ function (e, t, n) {
      Object.defineProperty(t, "__esModule", { value: !0 }),
        (t.default = void 0);
      var r = [
          "qy",
          "env",
          "error",
          "version",
          "lanDebug",
          "cloud",
          "serviceMarket",
          "router",
          "worklet",
          "__webpack_require_UNI_MP_PLUGIN__",
        ],
        a = ["lanDebug", "router", "worklet"],
        i =
          "undefined" != typeof globalThis
            ? globalThis
            : (function () {
                return this;
              })(),
        o = ["w", "x"].join(""),
        s = i[o],
        c = s.getLaunchOptionsSync ? s.getLaunchOptionsSync() : null;
      function u(e) {
        return (
          (!c || 1154 !== c.scene || !a.includes(e)) &&
          (r.indexOf(e) > -1 || "function" == typeof s[e])
        );
      }
      (i[o] = (function () {
        var e = {};
        for (var t in s) u(t) && (e[t] = s[t]);
        return e;
      })()),
        i[o].canIUse("getAppBaseInfo") ||
          (i[o].getAppBaseInfo = i[o].getSystemInfoSync),
        i[o].canIUse("getWindowInfo") ||
          (i[o].getWindowInfo = i[o].getSystemInfoSync),
        i[o].canIUse("getDeviceInfo") ||
          (i[o].getDeviceInfo = i[o].getSystemInfoSync);
      var l = i[o];
      t.default = l;
    },
    /*!************************************************************!*\
  !*** ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js ***!
  \************************************************************/
    /*! no static exports found */ function (e, t, n) {
      (function (e, r) {
        var a = n(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
        Object.defineProperty(t, "__esModule", { value: !0 }),
          (t.createApp = jt),
          (t.createComponent = Nt),
          (t.createPage = Ft),
          (t.createPlugin = Rt),
          (t.createSubpackageApp = Vt),
          (t.default = void 0);
        var i,
          o = a(n(/*! @babel/runtime/helpers/slicedToArray */ 5)),
          s = a(n(/*! @babel/runtime/helpers/defineProperty */ 11)),
          c = a(n(/*! @babel/runtime/helpers/construct */ 15)),
          u = a(n(/*! @babel/runtime/helpers/toConsumableArray */ 18)),
          l = a(n(/*! @babel/runtime/helpers/typeof */ 13)),
          p = n(/*! @dcloudio/uni-i18n */ 22),
          d = a(n(/*! vue */ 25));
        function f(e, t) {
          var n = Object.keys(e);
          if (Object.getOwnPropertySymbols) {
            var r = Object.getOwnPropertySymbols(e);
            t &&
              (r = r.filter(function (t) {
                return Object.getOwnPropertyDescriptor(e, t).enumerable;
              })),
              n.push.apply(n, r);
          }
          return n;
        }
        function _(e) {
          for (var t = 1; t < arguments.length; t++) {
            var n = null != arguments[t] ? arguments[t] : {};
            t % 2
              ? f(Object(n), !0).forEach(function (t) {
                  (0, s.default)(e, t, n[t]);
                })
              : Object.getOwnPropertyDescriptors
              ? Object.defineProperties(e, Object.getOwnPropertyDescriptors(n))
              : f(Object(n)).forEach(function (t) {
                  Object.defineProperty(
                    e,
                    t,
                    Object.getOwnPropertyDescriptor(n, t)
                  );
                });
          }
          return e;
        }
        var h =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
          m =
            /^(?:[A-Za-z\d+/]{4})*?(?:[A-Za-z\d+/]{2}(?:==)?|[A-Za-z\d+/]{3}=?)?$/;
        function v() {
          var t,
            n,
            r = e.getStorageSync("uni_id_token") || "",
            a = r.split(".");
          if (!r || 3 !== a.length)
            return { uid: null, role: [], permission: [], tokenExpired: 0 };
          try {
            t = JSON.parse(
              ((n = a[1]),
              decodeURIComponent(
                i(n)
                  .split("")
                  .map(function (e) {
                    return (
                      "%" + ("00" + e.charCodeAt(0).toString(16)).slice(-2)
                    );
                  })
                  .join("")
              ))
            );
          } catch (e) {
            throw new Error(
              "获取当前用户信息出错，详细错误信息为：" + e.message
            );
          }
          return (t.tokenExpired = 1e3 * t.exp), delete t.exp, delete t.iat, t;
        }
        i =
          "function" != typeof atob
            ? function (e) {
                if (((e = String(e).replace(/[\t\n\f\r ]+/g, "")), !m.test(e)))
                  throw new Error(
                    "Failed to execute 'atob' on 'Window': The string to be decoded is not correctly encoded."
                  );
                var t;
                e += "==".slice(2 - (3 & e.length));
                for (var n, r, a = "", i = 0; i < e.length; )
                  (t =
                    (h.indexOf(e.charAt(i++)) << 18) |
                    (h.indexOf(e.charAt(i++)) << 12) |
                    ((n = h.indexOf(e.charAt(i++))) << 6) |
                    (r = h.indexOf(e.charAt(i++)))),
                    (a +=
                      64 === n
                        ? String.fromCharCode((t >> 16) & 255)
                        : 64 === r
                        ? String.fromCharCode((t >> 16) & 255, (t >> 8) & 255)
                        : String.fromCharCode(
                            (t >> 16) & 255,
                            (t >> 8) & 255,
                            255 & t
                          ));
                return a;
              }
            : atob;
        var g = Object.prototype.toString,
          b = Object.prototype.hasOwnProperty;
        function y(e) {
          return "function" == typeof e;
        }
        function x(e) {
          return "string" == typeof e;
        }
        function w(e) {
          return "[object Object]" === g.call(e);
        }
        function S(e, t) {
          return b.call(e, t);
        }
        function O() {}
        function A(e) {
          var t = Object.create(null);
          return function (n) {
            return t[n] || (t[n] = e(n));
          };
        }
        var k = /-(\w)/g,
          $ = A(function (e) {
            return e.replace(k, function (e, t) {
              return t ? t.toUpperCase() : "";
            });
          });
        function E(e) {
          var t = {};
          return (
            w(e) &&
              Object.keys(e)
                .sort()
                .forEach(function (n) {
                  t[n] = e[n];
                }),
            Object.keys(t) ? t : e
          );
        }
        var j = ["invoke", "success", "fail", "complete", "returnValue"],
          P = {},
          C = {};
        function I(e, t) {
          Object.keys(t).forEach(function (n) {
            var r, a, i;
            -1 !== j.indexOf(n) &&
              y(t[n]) &&
              (e[n] =
                ((r = e[n]),
                (a = t[n]),
                (i = a ? (r ? r.concat(a) : Array.isArray(a) ? a : [a]) : r)
                  ? (function (e) {
                      for (var t = [], n = 0; n < e.length; n++)
                        -1 === t.indexOf(e[n]) && t.push(e[n]);
                      return t;
                    })(i)
                  : i));
          });
        }
        function T(e, t) {
          e &&
            t &&
            Object.keys(t).forEach(function (n) {
              -1 !== j.indexOf(n) &&
                y(t[n]) &&
                (function (e, t) {
                  var n = e.indexOf(t);
                  -1 !== n && e.splice(n, 1);
                })(e[n], t[n]);
            });
        }
        function L(e, t) {
          return function (n) {
            return e(n, t) || n;
          };
        }
        function D(e) {
          return (
            !!e &&
            ("object" === (0, l.default)(e) || "function" == typeof e) &&
            "function" == typeof e.then
          );
        }
        function M(e, t, n) {
          for (var r = !1, a = 0; a < e.length; a++) {
            var i = e[a];
            if (r) r = Promise.resolve(L(i, n));
            else {
              var o = i(t, n);
              if ((D(o) && (r = Promise.resolve(o)), !1 === o))
                return { then: function () {} };
            }
          }
          return (
            r || {
              then: function (e) {
                return e(t);
              },
            }
          );
        }
        function B(e) {
          var t =
            arguments.length > 1 && void 0 !== arguments[1] ? arguments[1] : {};
          return (
            ["success", "fail", "complete"].forEach(function (n) {
              if (Array.isArray(e[n])) {
                var r = t[n];
                t[n] = function (a) {
                  M(e[n], a, t).then(function (e) {
                    return (y(r) && r(e)) || e;
                  });
                };
              }
            }),
            t
          );
        }
        function F(e, t) {
          var n = [];
          Array.isArray(P.returnValue) &&
            n.push.apply(n, (0, u.default)(P.returnValue));
          var r = C[e];
          return (
            r &&
              Array.isArray(r.returnValue) &&
              n.push.apply(n, (0, u.default)(r.returnValue)),
            n.forEach(function (e) {
              t = e(t) || t;
            }),
            t
          );
        }
        function N(e) {
          var t = Object.create(null);
          Object.keys(P).forEach(function (e) {
            "returnValue" !== e && (t[e] = P[e].slice());
          });
          var n = C[e];
          return (
            n &&
              Object.keys(n).forEach(function (e) {
                "returnValue" !== e && (t[e] = (t[e] || []).concat(n[e]));
              }),
            t
          );
        }
        function V(e, t, n) {
          for (
            var r = arguments.length, a = new Array(r > 3 ? r - 3 : 0), i = 3;
            i < r;
            i++
          )
            a[i - 3] = arguments[i];
          var o = N(e);
          if (o && Object.keys(o).length) {
            if (Array.isArray(o.invoke)) {
              var s = M(o.invoke, n);
              return s.then(function (n) {
                return t.apply(void 0, [B(N(e), n)].concat(a));
              });
            }
            return t.apply(void 0, [B(o, n)].concat(a));
          }
          return t.apply(void 0, [n].concat(a));
        }
        var R = {
            returnValue: function (e) {
              return D(e)
                ? new Promise(function (t, n) {
                    e.then(function (e) {
                      e ? (e[0] ? n(e[0]) : t(e[1])) : t(e);
                    });
                  })
                : e;
            },
          },
          U =
            /^\$|__f__|Window$|WindowStyle$|sendHostEvent|sendNativeEvent|restoreGlobal|requireGlobal|getCurrentSubNVue|getMenuButtonBoundingClientRect|^report|interceptors|Interceptor$|getSubNVueById|requireNativePlugin|rpx2px|upx2px|hideKeyboard|canIUse|^create|Sync$|Manager$|base64ToArrayBuffer|arrayBufferToBase64|getLocale|setLocale|invokePushCallback|getWindowInfo|getDeviceInfo|getAppBaseInfo|getSystemSetting|getAppAuthorizeSetting|initUTS|requireUTS|registerUTS/,
          H = /^create|Manager$/,
          K = ["createBLEConnection"],
          q = ["createBLEConnection", "createPushMessage"],
          G = /^on|^off/;
        function z(e) {
          return H.test(e) && -1 === K.indexOf(e);
        }
        function J(e) {
          return U.test(e) && -1 === q.indexOf(e);
        }
        function W(e) {
          return e
            .then(function (e) {
              return [null, e];
            })
            .catch(function (e) {
              return [e];
            });
        }
        function X(e) {
          return !(
            z(e) ||
            J(e) ||
            (function (e) {
              return G.test(e) && "onPush" !== e;
            })(e)
          );
        }
        function Z(e, t) {
          return X(e) && y(t)
            ? function () {
                for (
                  var n =
                      arguments.length > 0 && void 0 !== arguments[0]
                        ? arguments[0]
                        : {},
                    r = arguments.length,
                    a = new Array(r > 1 ? r - 1 : 0),
                    i = 1;
                  i < r;
                  i++
                )
                  a[i - 1] = arguments[i];
                return y(n.success) || y(n.fail) || y(n.complete)
                  ? F(e, V.apply(void 0, [e, t, n].concat(a)))
                  : F(
                      e,
                      W(
                        new Promise(function (r, i) {
                          V.apply(
                            void 0,
                            [
                              e,
                              t,
                              Object.assign({}, n, { success: r, fail: i }),
                            ].concat(a)
                          );
                        })
                      )
                    );
              }
            : t;
        }
        Promise.prototype.finally ||
          (Promise.prototype.finally = function (e) {
            var t = this.constructor;
            return this.then(
              function (n) {
                return t.resolve(e()).then(function () {
                  return n;
                });
              },
              function (n) {
                return t.resolve(e()).then(function () {
                  throw n;
                });
              }
            );
          });
        var Y = !1,
          Q = 0,
          ee = 0;
        function te(t, n) {
          var r, a, i, o, s;
          if (
            (0 === Q &&
              ((o =
                "function" == typeof e.getWindowInfo && e.getWindowInfo()
                  ? e.getWindowInfo()
                  : e.getSystemInfoSync()),
              (s =
                "function" == typeof e.getDeviceInfo && e.getDeviceInfo()
                  ? e.getDeviceInfo()
                  : e.getSystemInfoSync()),
              (r = o.windowWidth),
              (a = o.pixelRatio),
              (i = s.platform),
              (Q = r),
              (ee = a),
              (Y = "ios" === i)),
            0 === (t = Number(t)))
          )
            return 0;
          var c = (t / 750) * (n || Q);
          return (
            c < 0 && (c = -c),
            0 === (c = Math.floor(c + 1e-4)) && (c = 1 !== ee && Y ? 0.5 : 1),
            t < 0 ? -c : c
          );
        }
        var ne,
          re = {};
        function ae() {
          var t =
            "function" == typeof e.getAppBaseInfo && e.getAppBaseInfo()
              ? e.getAppBaseInfo()
              : e.getSystemInfoSync();
          return se(t && t.language ? t.language : "en") || "en";
        }
        (ne = ae()),
          (function () {
            if (
              "undefined" != typeof __uniConfig &&
              __uniConfig.locales &&
              Object.keys(__uniConfig.locales).length
            ) {
              var e = Object.keys(__uniConfig.locales);
              e.length &&
                e.forEach(function (e) {
                  var t = re[e],
                    n = __uniConfig.locales[e];
                  t ? Object.assign(t, n) : (re[e] = n);
                });
            }
          })();
        var ie = (0, p.initVueI18n)(ne, {}),
          oe = ie.t;
        (ie.mixin = {
          beforeCreate: function () {
            var e = this,
              t = ie.i18n.watchLocale(function () {
                e.$forceUpdate();
              });
            this.$once("hook:beforeDestroy", function () {
              t();
            });
          },
          methods: {
            $$t: function (e, t) {
              return oe(e, t);
            },
          },
        }),
          ie.setLocale,
          ie.getLocale;
        function se(e, t) {
          if (e) {
            if (((e = e.trim().replace(/_/g, "-")), t && t[e])) return e;
            if ("chinese" === (e = e.toLowerCase())) return "zh-Hans";
            if (0 === e.indexOf("zh"))
              return e.indexOf("-hans") > -1
                ? "zh-Hans"
                : e.indexOf("-hant") > -1
                ? "zh-Hant"
                : ((n = e),
                  ["-tw", "-hk", "-mo", "-cht"].find(function (e) {
                    return -1 !== n.indexOf(e);
                  })
                    ? "zh-Hant"
                    : "zh-Hans");
            var n,
              r = (function (e, t) {
                return t.find(function (t) {
                  return 0 === e.indexOf(t);
                });
              })(e, ["en", "fr", "es"]);
            return r || void 0;
          }
        }
        function ce() {
          if (y(getApp)) {
            var e = getApp({ allowDefault: !0 });
            if (e && e.$vm) return e.$vm.$locale;
          }
          return ae();
        }
        var ue = [];
        void 0 !== r && (r.getLocale = ce);
        var le = { promiseInterceptor: R },
          pe = Object.freeze({
            __proto__: null,
            upx2px: te,
            rpx2px: te,
            getLocale: ce,
            setLocale: function (e) {
              var t = !!y(getApp) && getApp();
              return (
                !!t &&
                t.$vm.$locale !== e &&
                ((t.$vm.$locale = e),
                ue.forEach(function (t) {
                  return t({ locale: e });
                }),
                !0)
              );
            },
            onLocaleChange: function (e) {
              -1 === ue.indexOf(e) && ue.push(e);
            },
            addInterceptor: function (e, t) {
              "string" == typeof e && w(t)
                ? I(C[e] || (C[e] = {}), t)
                : w(e) && I(P, e);
            },
            removeInterceptor: function (e, t) {
              "string" == typeof e
                ? w(t)
                  ? T(C[e], t)
                  : delete C[e]
                : w(e) && T(P, e);
            },
            interceptors: le,
          });
        var de;
        function fe(t) {
          (de = de || e.getStorageSync("__DC_STAT_UUID")) ||
            ((de = Date.now() + "" + Math.floor(1e7 * Math.random())),
            e.setStorage({ key: "__DC_STAT_UUID", data: de })),
            (t.deviceId = de);
        }
        function _e(e) {
          if (e.safeArea) {
            var t = e.safeArea;
            e.safeAreaInsets = {
              top: t.top,
              left: t.left,
              right: e.windowWidth - t.right,
              bottom: e.screenHeight - t.bottom,
            };
          }
        }
        function he(e, t) {
          var n = "",
            r = "";
          switch (
            ((n = e.split(" ")[0] || t),
            (r = e.split(" ")[1] || ""),
            (n = n.toLocaleLowerCase()))
          ) {
            case "harmony":
            case "ohos":
            case "openharmony":
              n = "harmonyos";
              break;
            case "iphone os":
              n = "ios";
              break;
            case "mac":
            case "darwin":
              n = "macos";
              break;
            case "windows_nt":
              n = "windows";
          }
          return { osName: n, osVersion: r };
        }
        function me(e, t) {
          for (
            var n = e.deviceType || "phone",
              r = { ipad: "pad", windows: "pc", mac: "pc" },
              a = Object.keys(r),
              i = t.toLocaleLowerCase(),
              o = 0;
            o < a.length;
            o++
          ) {
            var s = a[o];
            if (-1 !== i.indexOf(s)) {
              n = r[s];
              break;
            }
          }
          return n;
        }
        function ve(e) {
          var t = e;
          return t && (t = e.toLocaleLowerCase()), t;
        }
        function ge(e) {
          return ce ? ce() : e;
        }
        function be(e) {
          var t = e.hostName || "WeChat";
          return (
            e.environment
              ? (t = e.environment)
              : e.host && e.host.env && (t = e.host.env),
            t
          );
        }
        var ye = {
            returnValue: function (e) {
              fe(e),
                _e(e),
                (function (e) {
                  var t = e.brand,
                    n = void 0 === t ? "" : t,
                    r = e.model,
                    a = void 0 === r ? "" : r,
                    i = e.system,
                    o = void 0 === i ? "" : i,
                    s = e.language,
                    c = void 0 === s ? "" : s,
                    u = e.theme,
                    l = e.version,
                    p = e.platform,
                    d = e.fontSizeSetting,
                    f = e.SDKVersion,
                    _ = e.pixelRatio,
                    h = e.deviceOrientation,
                    m = he(o, p),
                    v = m.osName,
                    g = m.osVersion,
                    b = l,
                    y = me(e, a),
                    x = ve(n),
                    w = be(e),
                    S = h,
                    O = _,
                    A = f,
                    k = (c || "").replace(/_/g, "-"),
                    $ = {
                      appId: "__UNI__EF9C31D",
                      appName: "GEKOO",
                      appVersion: "1.0.2",
                      appVersionCode: "102",
                      appLanguage: ge(k),
                      uniCompileVersion: "4.66",
                      uniCompilerVersion: "4.66",
                      uniRuntimeVersion: "4.66",
                      uniPlatform: "mp-weixin",
                      deviceBrand: x,
                      deviceModel: a,
                      deviceType: y,
                      devicePixelRatio: O,
                      deviceOrientation: S,
                      osName: v.toLocaleLowerCase(),
                      osVersion: g,
                      hostTheme: u,
                      hostVersion: b,
                      hostLanguage: k,
                      hostName: w,
                      hostSDKVersion: A,
                      hostFontSizeSetting: d,
                      windowTop: 0,
                      windowBottom: 0,
                      osLanguage: void 0,
                      osTheme: void 0,
                      ua: void 0,
                      hostPackageName: void 0,
                      browserName: void 0,
                      browserVersion: void 0,
                      isUniAppX: !1,
                    };
                  Object.assign(e, $, {});
                })(e);
            },
          },
          xe = {
            redirectTo: {
              name: function (e) {
                return "back" === e.exists && e.delta
                  ? "navigateBack"
                  : "redirectTo";
              },
              args: function (e) {
                if ("back" === e.exists && e.url) {
                  var t = (function (e) {
                    for (var t = getCurrentPages(), n = t.length; n--; ) {
                      var r = t[n];
                      if (r.$page && r.$page.fullPath === e) return n;
                    }
                    return -1;
                  })(e.url);
                  if (-1 !== t) {
                    var n = getCurrentPages().length - 1 - t;
                    n > 0 && (e.delta = n);
                  }
                }
              },
            },
            previewImage: {
              args: function (e) {
                var t = parseInt(e.current);
                if (!isNaN(t)) {
                  var n = e.urls;
                  if (Array.isArray(n)) {
                    var r = n.length;
                    if (r)
                      return (
                        t < 0 ? (t = 0) : t >= r && (t = r - 1),
                        t > 0
                          ? ((e.current = n[t]),
                            (e.urls = n.filter(function (e, r) {
                              return !(r < t) || e !== n[t];
                            })))
                          : (e.current = n[0]),
                        { indicator: !1, loop: !1 }
                      );
                  }
                }
              },
            },
            getSystemInfo: ye,
            getSystemInfoSync: ye,
            showActionSheet: {
              args: function (e) {
                "object" === (0, l.default)(e) && (e.alertText = e.title);
              },
            },
            getAppBaseInfo: {
              returnValue: function (e) {
                var t = e,
                  n = t.version,
                  r = t.language,
                  a = t.SDKVersion,
                  i = t.theme,
                  o = be(e),
                  s = (r || "").replace("_", "-");
                e = E(
                  Object.assign(e, {
                    appId: "__UNI__EF9C31D",
                    appName: "GEKOO",
                    appVersion: "1.0.2",
                    appVersionCode: "102",
                    appLanguage: ge(s),
                    hostVersion: n,
                    hostLanguage: s,
                    hostName: o,
                    hostSDKVersion: a,
                    hostTheme: i,
                    isUniAppX: !1,
                    uniPlatform: "mp-weixin",
                    uniCompileVersion: "4.66",
                    uniCompilerVersion: "4.66",
                    uniRuntimeVersion: "4.66",
                  })
                );
              },
            },
            getDeviceInfo: {
              returnValue: function (e) {
                var t = e,
                  n = t.brand,
                  r = t.model,
                  a = t.system,
                  i = void 0 === a ? "" : a,
                  o = t.platform,
                  s = void 0 === o ? "" : o,
                  c = me(e, r),
                  u = ve(n);
                fe(e);
                var l = he(i, s),
                  p = l.osName,
                  d = l.osVersion;
                e = E(
                  Object.assign(e, {
                    deviceType: c,
                    deviceBrand: u,
                    deviceModel: r,
                    osName: p,
                    osVersion: d,
                  })
                );
              },
            },
            getWindowInfo: {
              returnValue: function (e) {
                _e(e),
                  (e = E(Object.assign(e, { windowTop: 0, windowBottom: 0 })));
              },
            },
            getAppAuthorizeSetting: {
              returnValue: function (e) {
                var t = e.locationReducedAccuracy;
                (e.locationAccuracy = "unsupported"),
                  !0 === t
                    ? (e.locationAccuracy = "reduced")
                    : !1 === t && (e.locationAccuracy = "full");
              },
            },
            compressImage: {
              args: function (e) {
                e.compressedHeight &&
                  !e.compressHeight &&
                  (e.compressHeight = e.compressedHeight),
                  e.compressedWidth &&
                    !e.compressWidth &&
                    (e.compressWidth = e.compressedWidth);
              },
            },
          },
          we = ["success", "fail", "cancel", "complete"];
        function Se(e, t, n) {
          return function (r) {
            return t(Ae(e, r, n));
          };
        }
        function Oe(e, t) {
          var n =
              arguments.length > 2 && void 0 !== arguments[2]
                ? arguments[2]
                : {},
            r =
              arguments.length > 3 && void 0 !== arguments[3]
                ? arguments[3]
                : {},
            a = arguments.length > 4 && void 0 !== arguments[4] && arguments[4];
          if (w(t)) {
            var i = !0 === a ? t : {};
            for (var o in (y(n) && (n = n(t, i) || {}), t))
              if (S(n, o)) {
                var s = n[o];
                y(s) && (s = s(t[o], t, i)),
                  s
                    ? x(s)
                      ? (i[s] = t[o])
                      : w(s) && (i[s.name ? s.name : o] = s.value)
                    : console.warn(
                        "The '"
                          .concat(
                            e,
                            "' method of platform '微信小程序' does not support option '"
                          )
                          .concat(o, "'")
                      );
              } else
                -1 !== we.indexOf(o)
                  ? y(t[o]) && (i[o] = Se(e, t[o], r))
                  : a || (i[o] = t[o]);
            return i;
          }
          return y(t) && (t = Se(e, t, r)), t;
        }
        function Ae(e, t, n) {
          var r =
            arguments.length > 3 && void 0 !== arguments[3] && arguments[3];
          return (
            y(xe.returnValue) && (t = xe.returnValue(e, t)), Oe(e, t, n, {}, r)
          );
        }
        function ke(t, n) {
          if (S(xe, t)) {
            var r = xe[t];
            return r
              ? function (n, a) {
                  var i = r;
                  y(r) && (i = r(n));
                  var o = [(n = Oe(t, n, i.args, i.returnValue))];
                  void 0 !== a && o.push(a),
                    y(i.name) ? (t = i.name(n)) : x(i.name) && (t = i.name);
                  var s = e[t].apply(e, o);
                  return J(t) ? Ae(t, s, i.returnValue, z(t)) : s;
                }
              : function () {
                  console.error(
                    "Platform '微信小程序' does not support '".concat(t, "'.")
                  );
                };
          }
          return n;
        }
        var $e = Object.create(null);
        [
          "onTabBarMidButtonTap",
          "subscribePush",
          "unsubscribePush",
          "onPush",
          "offPush",
          "share",
        ].forEach(function (e) {
          $e[e] = (function (e) {
            return function (t) {
              var n = t.fail,
                r = t.complete,
                a = {
                  errMsg: ""
                    .concat(e, ":fail method '")
                    .concat(e, "' not supported"),
                };
              y(n) && n(a), y(r) && r(a);
            };
          })(e);
        });
        var Ee = {
          oauth: ["weixin"],
          share: ["weixin"],
          payment: ["wxpay"],
          push: ["weixin"],
        };
        var je,
          Pe = Object.freeze({
            __proto__: null,
            getProvider: function (e) {
              var t = e.service,
                n = e.success,
                r = e.fail,
                a = e.complete,
                i = !1;
              Ee[t]
                ? ((i = {
                    errMsg: "getProvider:ok",
                    service: t,
                    provider: Ee[t],
                  }),
                  y(n) && n(i))
                : ((i = { errMsg: "getProvider:fail service not found" }),
                  y(r) && r(i)),
                y(a) && a(i);
            },
          }),
          Ce = function () {
            return je || (je = new d.default()), je;
          };
        function Ie(e, t, n) {
          return e[t].apply(e, n);
        }
        var Te,
          Le,
          De,
          Me = Object.freeze({
            __proto__: null,
            $on: function () {
              return Ie(Ce(), "$on", Array.prototype.slice.call(arguments));
            },
            $off: function () {
              return Ie(Ce(), "$off", Array.prototype.slice.call(arguments));
            },
            $once: function () {
              return Ie(Ce(), "$once", Array.prototype.slice.call(arguments));
            },
            $emit: function () {
              return Ie(Ce(), "$emit", Array.prototype.slice.call(arguments));
            },
          });
        function Be(e) {
          return function () {
            try {
              return e.apply(e, arguments);
            } catch (e) {
              console.error(e);
            }
          };
        }
        function Fe(e) {
          try {
            return JSON.parse(e);
          } catch (e) {}
          return e;
        }
        var Ne = [];
        function Ve(e, t) {
          Ne.forEach(function (n) {
            n(e, t);
          }),
            (Ne.length = 0);
        }
        var Re = [];
        var Ue = e.getAppBaseInfo && e.getAppBaseInfo();
        Ue || (Ue = e.getSystemInfoSync());
        var He = Ue ? Ue.host : null,
          Ke =
            He && "SAAASDK" === He.env
              ? e.miniapp.shareVideoMessage
              : e.shareVideoMessage,
          qe = Object.freeze({
            __proto__: null,
            shareVideoMessage: Ke,
            getPushClientId: function (e) {
              w(e) || (e = {});
              var t = (function (e) {
                  var t = {};
                  for (var n in e) {
                    var r = e[n];
                    y(r) && ((t[n] = Be(r)), delete e[n]);
                  }
                  return t;
                })(e),
                n = t.success,
                r = t.fail,
                a = t.complete,
                i = y(n),
                o = y(r),
                s = y(a);
              Promise.resolve().then(function () {
                void 0 === De &&
                  ((De = !1), (Te = ""), (Le = "uniPush is not enabled")),
                  Ne.push(function (e, t) {
                    var c;
                    e
                      ? ((c = { errMsg: "getPushClientId:ok", cid: e }),
                        i && n(c))
                      : ((c = {
                          errMsg: "getPushClientId:fail" + (t ? " " + t : ""),
                        }),
                        o && r(c)),
                      s && a(c);
                  }),
                  void 0 !== Te && Ve(Te, Le);
              });
            },
            onPushMessage: function (e) {
              -1 === Re.indexOf(e) && Re.push(e);
            },
            offPushMessage: function (e) {
              if (e) {
                var t = Re.indexOf(e);
                t > -1 && Re.splice(t, 1);
              } else Re.length = 0;
            },
            invokePushCallback: function (e) {
              if ("enabled" === e.type) De = !0;
              else if ("clientId" === e.type)
                (Te = e.cid), (Le = e.errMsg), Ve(Te, e.errMsg);
              else if ("pushMsg" === e.type)
                for (
                  var t = { type: "receive", data: Fe(e.message) }, n = 0;
                  n < Re.length;
                  n++
                ) {
                  if (((0, Re[n])(t), t.stopped)) break;
                }
              else
                "click" === e.type &&
                  Re.forEach(function (t) {
                    t({ type: "click", data: Fe(e.message) });
                  });
            },
            __f__: function (e) {
              for (
                var t = arguments.length,
                  n = new Array(t > 1 ? t - 1 : 0),
                  r = 1;
                r < t;
                r++
              )
                n[r - 1] = arguments[r];
              console[e].apply(console, n);
            },
          }),
          Ge = ["__route__", "__wxExparserNodeId__", "__wxWebviewId__"];
        function ze(e) {
          return Behavior(e);
        }
        function Je() {
          return !!this.route;
        }
        function We(e) {
          this.triggerEvent("__l", e);
        }
        function Xe(e) {
          var t = e.$scope,
            n = {};
          Object.defineProperty(e, "$refs", {
            get: function () {
              var e = {};
              return (
                (function e(t, n, r) {
                  (t.selectAllComponents(n) || []).forEach(function (t) {
                    var a = t.dataset.ref;
                    (r[a] = t.$vm || Qe(t)),
                      "scoped" === t.dataset.vueGeneric &&
                        t
                          .selectAllComponents(".scoped-ref")
                          .forEach(function (t) {
                            e(t, n, r);
                          });
                  });
                })(t, ".vue-ref", e),
                (t.selectAllComponents(".vue-ref-in-for") || []).forEach(
                  function (t) {
                    var n = t.dataset.ref;
                    e[n] || (e[n] = []), e[n].push(t.$vm || Qe(t));
                  }
                ),
                (function (e, t) {
                  var n = (0, c.default)(Set, (0, u.default)(Object.keys(e)));
                  return (
                    Object.keys(t).forEach(function (r) {
                      var a = e[r],
                        i = t[r];
                      (Array.isArray(a) &&
                        Array.isArray(i) &&
                        a.length === i.length &&
                        i.every(function (e) {
                          return a.includes(e);
                        })) ||
                        ((e[r] = i), n.delete(r));
                    }),
                    n.forEach(function (t) {
                      delete e[t];
                    }),
                    e
                  );
                })(n, e)
              );
            },
          });
        }
        function Ze(e) {
          var t,
            n = e.detail || e.value,
            r = n.vuePid,
            a = n.vueOptions;
          r &&
            (t = (function e(t, n) {
              for (var r, a = t.$children, i = a.length - 1; i >= 0; i--) {
                var o = a[i];
                if (o.$scope._$vueId === n) return o;
              }
              for (var s = a.length - 1; s >= 0; s--)
                if ((r = e(a[s], n))) return r;
            })(this.$vm, r)),
            t || (t = this.$vm),
            (a.parent = t);
        }
        function Ye(e) {
          return (
            Object.defineProperty(e, "__v_isMPComponent", {
              configurable: !0,
              enumerable: !1,
              value: !0,
            }),
            e
          );
        }
        function Qe(e) {
          return (
            (function (e) {
              return null !== e && "object" === (0, l.default)(e);
            })(e) &&
              Object.isExtensible(e) &&
              Object.defineProperty(e, "__ob__", {
                configurable: !0,
                enumerable: !1,
                value: (0, s.default)({}, "__v_skip", !0),
              }),
            e
          );
        }
        var et = /_(.*)_worklet_factory_/;
        var tt = Page,
          nt = Component,
          rt = /:/g,
          at = A(function (e) {
            return $(e.replace(rt, "-"));
          });
        function it(e) {
          var t = e.triggerEvent,
            n = function (e) {
              for (
                var n = arguments.length,
                  r = new Array(n > 1 ? n - 1 : 0),
                  a = 1;
                a < n;
                a++
              )
                r[a - 1] = arguments[a];
              if (this.$vm || (this.dataset && this.dataset.comType)) e = at(e);
              else {
                var i = at(e);
                i !== e && t.apply(this, [i].concat(r));
              }
              return t.apply(this, [e].concat(r));
            };
          try {
            e.triggerEvent = n;
          } catch (t) {
            e._triggerEvent = n;
          }
        }
        function ot(e, t, n) {
          var r = t[e];
          t[e] = function () {
            if ((Ye(this), it(this), r)) {
              for (
                var e = arguments.length, t = new Array(e), n = 0;
                n < e;
                n++
              )
                t[n] = arguments[n];
              return r.apply(this, t);
            }
          };
        }
        tt.__$wrappered ||
          ((tt.__$wrappered = !0),
          (Page = function () {
            var e =
              arguments.length > 0 && void 0 !== arguments[0]
                ? arguments[0]
                : {};
            return ot("onLoad", e), tt(e);
          }),
          (Page.after = tt.after),
          (Component = function () {
            var e =
              arguments.length > 0 && void 0 !== arguments[0]
                ? arguments[0]
                : {};
            return ot("created", e), nt(e);
          }));
        function st(e, t, n) {
          t.forEach(function (t) {
            (function e(t, n) {
              if (!n) return !0;
              if (d.default.options && Array.isArray(d.default.options[t]))
                return !0;
              if (y((n = n.default || n)))
                return (
                  !!y(n.extendOptions[t]) ||
                  !!(
                    n.super &&
                    n.super.options &&
                    Array.isArray(n.super.options[t])
                  )
                );
              if (y(n[t]) || Array.isArray(n[t])) return !0;
              var r = n.mixins;
              return Array.isArray(r)
                ? !!r.find(function (n) {
                    return e(t, n);
                  })
                : void 0;
            })(t, n) &&
              (e[t] = function (e) {
                return this.$vm && this.$vm.__call_hook(t, e);
              });
          });
        }
        function ct(e, t) {
          var n =
            arguments.length > 2 && void 0 !== arguments[2] ? arguments[2] : [];
          ut(t).forEach(function (t) {
            return lt(e, t, n);
          });
        }
        function ut(e) {
          var t =
            arguments.length > 1 && void 0 !== arguments[1] ? arguments[1] : [];
          return (
            e &&
              Object.keys(e).forEach(function (n) {
                0 === n.indexOf("on") && y(e[n]) && t.push(n);
              }),
            t
          );
        }
        function lt(e, t, n) {
          -1 !== n.indexOf(t) ||
            S(e, t) ||
            (e[t] = function (e) {
              return this.$vm && this.$vm.__call_hook(t, e);
            });
        }
        function pt(e, t) {
          var n;
          return [
            (n = y((t = t.default || t)) ? t : e.extend(t)),
            (t = n.options),
          ];
        }
        function dt(e, t) {
          if (Array.isArray(t) && t.length) {
            var n = Object.create(null);
            t.forEach(function (e) {
              n[e] = !0;
            }),
              (e.$scopedSlots = e.$slots = n);
          }
        }
        function ft(e, t) {
          var n = (e = (e || "").split(",")).length;
          1 === n
            ? (t._$vueId = e[0])
            : 2 === n && ((t._$vueId = e[0]), (t._$vuePid = e[1]));
        }
        function _t(e, t) {
          var n = e.data || {},
            r = e.methods || {};
          if ("function" == typeof n)
            try {
              n = n.call(t);
            } catch (e) {
              Object({
                NODE_ENV: "development",
                VUE_APP_DARK_MODE: "false",
                VUE_APP_NAME: "GEKOO",
                VUE_APP_PLATFORM: "mp-weixin",
                BASE_URL: "/",
              }).VUE_APP_DEBUG &&
                console.warn(
                  "根据 Vue 的 data 函数初始化小程序 data 失败，请尽量确保 data 函数中不访问 vm 对象，否则可能影响首次数据渲染速度。",
                  n
                );
            }
          else
            try {
              n = JSON.parse(JSON.stringify(n));
            } catch (e) {}
          return (
            w(n) || (n = {}),
            Object.keys(r).forEach(function (e) {
              -1 !== t.__lifecycle_hooks__.indexOf(e) ||
                S(n, e) ||
                (n[e] = r[e]);
            }),
            n
          );
        }
        var ht = [String, Number, Boolean, Object, Array, null];
        function mt(e) {
          return function (t, n) {
            this.$vm && (this.$vm[e] = t);
          };
        }
        function vt(e, t) {
          var n = e.behaviors,
            r = e.extends,
            a = e.mixins,
            i = e.props;
          i || (e.props = i = []);
          var o = [];
          return (
            Array.isArray(n) &&
              n.forEach(function (e) {
                o.push(e.replace("uni://", "wx".concat("://"))),
                  "uni://form-field" === e &&
                    (Array.isArray(i)
                      ? (i.push("name"), i.push("value"))
                      : ((i.name = { type: String, default: "" }),
                        (i.value = {
                          type: [String, Number, Boolean, Array, Object, Date],
                          default: "",
                        })));
              }),
            w(r) && r.props && o.push(t({ properties: bt(r.props, !0) })),
            Array.isArray(a) &&
              a.forEach(function (e) {
                w(e) && e.props && o.push(t({ properties: bt(e.props, !0) }));
              }),
            o
          );
        }
        function gt(e, t, n, r) {
          return Array.isArray(t) && 1 === t.length ? t[0] : t;
        }
        function bt(e) {
          var t =
              arguments.length > 1 && void 0 !== arguments[1] && arguments[1],
            n = arguments.length > 3 ? arguments[3] : void 0,
            r = {};
          return (
            t ||
              ((r.vueId = { type: String, value: "" }),
              n.virtualHost &&
                ((r.virtualHostStyle = { type: null, value: "" }),
                (r.virtualHostClass = { type: null, value: "" })),
              (r.scopedSlotsCompiler = { type: String, value: "" }),
              (r.vueSlots = {
                type: null,
                value: [],
                observer: function (e, t) {
                  var n = Object.create(null);
                  e.forEach(function (e) {
                    n[e] = !0;
                  }),
                    this.setData({ $slots: n });
                },
              })),
            Array.isArray(e)
              ? e.forEach(function (e) {
                  r[e] = { type: null, observer: mt(e) };
                })
              : w(e) &&
                Object.keys(e).forEach(function (t) {
                  var n = e[t];
                  if (w(n)) {
                    var a = n.default;
                    y(a) && (a = a()),
                      (n.type = gt(0, n.type)),
                      (r[t] = {
                        type: -1 !== ht.indexOf(n.type) ? n.type : null,
                        value: a,
                        observer: mt(t),
                      });
                  } else {
                    var i = gt(0, n);
                    r[t] = {
                      type: -1 !== ht.indexOf(i) ? i : null,
                      observer: mt(t),
                    };
                  }
                }),
            r
          );
        }
        function yt(e, t, n, r) {
          var a = {};
          return (
            Array.isArray(t) &&
              t.length &&
              t.forEach(function (t, i) {
                "string" == typeof t
                  ? t
                    ? "$event" === t
                      ? (a["$" + i] = n)
                      : "arguments" === t
                      ? (a["$" + i] = (n.detail && n.detail.__args__) || r)
                      : 0 === t.indexOf("$event.")
                      ? (a["$" + i] = e.__get_value(
                          t.replace("$event.", ""),
                          n
                        ))
                      : (a["$" + i] = e.__get_value(t))
                    : (a["$" + i] = e)
                  : (a["$" + i] = (function (e, t) {
                      var n = e;
                      return (
                        t.forEach(function (t) {
                          var r = t[0],
                            a = t[2];
                          if (r || void 0 !== a) {
                            var i,
                              o = t[1],
                              s = t[3];
                            Number.isInteger(r)
                              ? (i = r)
                              : r
                              ? "string" == typeof r &&
                                r &&
                                (i =
                                  0 === r.indexOf("#s#")
                                    ? r.substr(3)
                                    : e.__get_value(r, n))
                              : (i = n),
                              Number.isInteger(i)
                                ? (n = a)
                                : o
                                ? Array.isArray(i)
                                  ? (n = i.find(function (t) {
                                      return e.__get_value(o, t) === a;
                                    }))
                                  : w(i)
                                  ? (n = Object.keys(i).find(function (t) {
                                      return e.__get_value(o, i[t]) === a;
                                    }))
                                  : console.error("v-for 暂不支持循环数据：", i)
                                : (n = i[a]),
                              s && (n = e.__get_value(s, n));
                          }
                        }),
                        n
                      );
                    })(e, t));
              }),
            a
          );
        }
        function xt(e) {
          for (var t = {}, n = 1; n < e.length; n++) {
            var r = e[n];
            t[r[0]] = r[1];
          }
          return t;
        }
        function wt(e, t) {
          var n =
              arguments.length > 2 && void 0 !== arguments[2]
                ? arguments[2]
                : [],
            r =
              arguments.length > 3 && void 0 !== arguments[3]
                ? arguments[3]
                : [],
            a = arguments.length > 4 ? arguments[4] : void 0,
            i = arguments.length > 5 ? arguments[5] : void 0,
            o = !1,
            s = (w(t.detail) && t.detail.__args__) || [t.detail];
          if (
            a &&
            ((o =
              t.currentTarget &&
              t.currentTarget.dataset &&
              "wx" === t.currentTarget.dataset.comType),
            !n.length)
          )
            return o ? [t] : s;
          var c = yt(e, r, t, s),
            u = [];
          return (
            n.forEach(function (e) {
              "$event" === e
                ? "__set_model" !== i || a
                  ? a && !o
                    ? u.push(s[0])
                    : u.push(t)
                  : u.push(t.target.value)
                : Array.isArray(e) && "o" === e[0]
                ? u.push(xt(e))
                : "string" == typeof e && S(c, e)
                ? u.push(c[e])
                : u.push(e);
            }),
            u
          );
        }
        function St(e) {
          var t = this,
            n = (
              (e = (function (e) {
                try {
                  e.mp = JSON.parse(JSON.stringify(e));
                } catch (e) {}
                return (
                  (e.stopPropagation = O),
                  (e.preventDefault = O),
                  (e.target = e.target || {}),
                  S(e, "detail") || (e.detail = {}),
                  S(e, "markerId") &&
                    ((e.detail =
                      "object" === (0, l.default)(e.detail) ? e.detail : {}),
                    (e.detail.markerId = e.markerId)),
                  w(e.detail) &&
                    (e.target = Object.assign({}, e.target, e.detail)),
                  e
                );
              })(e)).currentTarget || e.target
            ).dataset;
          if (!n) return console.warn("事件信息不存在");
          var r = n.eventOpts || n["event-opts"];
          if (!r) return console.warn("事件信息不存在");
          var a = e.type,
            i = [];
          return (
            r.forEach(function (n) {
              var r = n[0],
                o = n[1],
                s = "^" === r.charAt(0),
                c = "~" === (r = s ? r.slice(1) : r).charAt(0);
              (r = c ? r.slice(1) : r),
                o &&
                  (function (e, t) {
                    return (
                      e === t ||
                      ("regionchange" === t && ("begin" === e || "end" === e))
                    );
                  })(a, r) &&
                  o.forEach(function (n) {
                    var r = n[0];
                    if (r) {
                      var a = t.$vm;
                      if (
                        (a.$options.generic &&
                          (a =
                            (function (e) {
                              for (
                                var t = e.$parent;
                                t &&
                                t.$parent &&
                                (t.$options.generic ||
                                  t.$parent.$options.generic ||
                                  t.$scope._$vuePid);

                              )
                                t = t.$parent;
                              return t && t.$parent;
                            })(a) || a),
                        "$emit" === r)
                      )
                        return void a.$emit.apply(
                          a,
                          wt(t.$vm, e, n[1], n[2], s, r)
                        );
                      var o = a[r];
                      if (!y(o)) {
                        var u = "page" === t.$vm.mpType ? "Page" : "Component",
                          l = t.route || t.is;
                        throw new Error(
                          ""
                            .concat(u, ' "')
                            .concat(l, '" does not have a method "')
                            .concat(r, '"')
                        );
                      }
                      if (c) {
                        if (o.once) return;
                        o.once = !0;
                      }
                      var p = wt(t.$vm, e, n[1], n[2], s, r);
                      (p = Array.isArray(p) ? p : []),
                        /=\s*\S+\.eventParams\s*\|\|\s*\S+\[['"]event-params['"]\]/.test(
                          o.toString()
                        ) && (p = p.concat([, , , , , , , , , , e])),
                        i.push(o.apply(a, p));
                    }
                  });
            }),
            "input" === a && 1 === i.length && void 0 !== i[0] ? i[0] : void 0
          );
        }
        var Ot = {};
        var At = [
          "onShow",
          "onHide",
          "onError",
          "onPageNotFound",
          "onThemeChange",
          "onUnhandledRejection",
        ];
        function kt() {
          d.default.prototype.getOpenerEventChannel = function () {
            return this.$scope.getOpenerEventChannel();
          };
          var e = d.default.prototype.__call_hook;
          d.default.prototype.__call_hook = function (t, n) {
            var r, a;
            return (
              "onLoad" === t &&
                n &&
                n.__id__ &&
                ((this.__eventChannel__ =
                  ((r = n.__id__), (a = Ot[r]), delete Ot[r], a)),
                delete n.__id__),
              e.call(this, t, n)
            );
          };
        }
        function $t(t, n) {
          var r,
            a = n.mocks,
            i = n.initRefs;
          kt(),
            (function () {
              var e = {},
                t = {};
              function n(e) {
                var t = this.$options.propsData.vueId;
                t && e(t.split(",")[0]);
              }
              (d.default.prototype.$hasSSP = function (n) {
                var r = e[n];
                return (
                  r ||
                    ((t[n] = this),
                    this.$on("hook:destroyed", function () {
                      delete t[n];
                    })),
                  r
                );
              }),
                (d.default.prototype.$getSSP = function (t, n, r) {
                  var a = e[t];
                  if (a) {
                    var i = a[n] || [];
                    return r ? i : i[0];
                  }
                }),
                (d.default.prototype.$setSSP = function (t, r) {
                  var a = 0;
                  return (
                    n.call(this, function (n) {
                      var i = e[n],
                        o = (i[t] = i[t] || []);
                      o.push(r), (a = o.length - 1);
                    }),
                    a
                  );
                }),
                (d.default.prototype.$initSSP = function () {
                  n.call(this, function (t) {
                    e[t] = {};
                  });
                }),
                (d.default.prototype.$callSSP = function () {
                  n.call(this, function (e) {
                    t[e] && t[e].$forceUpdate();
                  });
                }),
                d.default.mixin({
                  destroyed: function () {
                    var n = this.$options.propsData,
                      r = n && n.vueId;
                    r && (delete e[r], delete t[r]);
                  },
                });
            })(),
            t.$options.store && (d.default.prototype.$store = t.$options.store),
            ((r = d.default).prototype.uniIDHasRole = function (e) {
              return v().role.indexOf(e) > -1;
            }),
            (r.prototype.uniIDHasPermission = function (e) {
              var t = v().permission;
              return this.uniIDHasRole("admin") || t.indexOf(e) > -1;
            }),
            (r.prototype.uniIDTokenValid = function () {
              return v().tokenExpired > Date.now();
            }),
            (d.default.prototype.mpHost = "mp-weixin"),
            d.default.mixin({
              beforeCreate: function () {
                if (this.$options.mpType) {
                  if (
                    ((this.mpType = this.$options.mpType),
                    (this.$mp = (0, s.default)(
                      { data: {} },
                      this.mpType,
                      this.$options.mpInstance
                    )),
                    (this.$scope = this.$options.mpInstance),
                    delete this.$options.mpType,
                    delete this.$options.mpInstance,
                    "page" === this.mpType && "function" == typeof getApp)
                  ) {
                    var e = getApp();
                    e.$vm && e.$vm.$i18n && (this._i18n = e.$vm.$i18n);
                  }
                  "app" !== this.mpType &&
                    (i(this),
                    (function (e, t) {
                      var n = e.$mp[e.mpType];
                      t.forEach(function (t) {
                        S(n, t) && (e[t] = n[t]);
                      });
                    })(this, a));
                }
              },
            });
          var o = {
            onLaunch: function (n) {
              this.$vm ||
                (e.canIUse &&
                  !e.canIUse("nextTick") &&
                  console.error(
                    "当前微信基础库版本过低，请将 微信开发者工具-详情-项目设置-调试基础库版本 更换为`2.3.0`以上"
                  ),
                (this.$vm = t),
                (this.$vm.$mp = { app: this }),
                (this.$vm.$scope = this),
                (this.$vm.globalData = this.globalData),
                (this.$vm._isMounted = !0),
                this.$vm.__call_hook("mounted", n),
                this.$vm.__call_hook("onLaunch", n));
            },
          };
          o.globalData = t.$options.globalData || {};
          var c,
            u = t.$options.methods;
          return (
            u &&
              Object.keys(u).forEach(function (e) {
                o[e] = u[e];
              }),
            (function (e, t, n) {
              var r = e.observable({ locale: n || ie.getLocale() }),
                a = [];
              (t.$watchLocale = function (e) {
                a.push(e);
              }),
                Object.defineProperty(t, "$locale", {
                  get: function () {
                    return r.locale;
                  },
                  set: function (e) {
                    (r.locale = e),
                      a.forEach(function (t) {
                        return t(e);
                      });
                  },
                });
            })(
              d.default,
              t,
              ((c = e.getAppBaseInfo()),
              se(c && c.language ? c.language : "en") || "en")
            ),
            st(o, At),
            ct(o, t.$options),
            o
          );
        }
        function Et(e) {
          return $t(e, { mocks: Ge, initRefs: Xe });
        }
        function jt(e) {
          return App(Et(e)), e;
        }
        var Pt = /[!'()*]/g,
          Ct = function (e) {
            return "%" + e.charCodeAt(0).toString(16);
          },
          It = /%2C/g,
          Tt = function (e) {
            return encodeURIComponent(e).replace(Pt, Ct).replace(It, ",");
          };
        function Lt(e) {
          var t =
              arguments.length > 1 && void 0 !== arguments[1]
                ? arguments[1]
                : Tt,
            n = e
              ? Object.keys(e)
                  .map(function (n) {
                    var r = e[n];
                    if (void 0 === r) return "";
                    if (null === r) return t(n);
                    if (Array.isArray(r)) {
                      var a = [];
                      return (
                        r.forEach(function (e) {
                          void 0 !== e &&
                            (null === e
                              ? a.push(t(n))
                              : a.push(t(n) + "=" + t(e)));
                        }),
                        a.join("&")
                      );
                    }
                    return t(n) + "=" + t(r);
                  })
                  .filter(function (e) {
                    return e.length > 0;
                  })
                  .join("&")
              : null;
          return n ? "?".concat(n) : "";
        }
        function Dt(e, t) {
          return (function (e) {
            var t =
                arguments.length > 1 && void 0 !== arguments[1]
                  ? arguments[1]
                  : {},
              n = t.isPage,
              r = t.initRelation,
              a = arguments.length > 2 ? arguments[2] : void 0,
              i = pt(d.default, e),
              s = (0, o.default)(i, 2),
              c = s[0],
              u = s[1],
              l = _({ multipleSlots: !0, addGlobalClass: !0 }, u.options || {});
            u["mp-weixin"] &&
              u["mp-weixin"].options &&
              Object.assign(l, u["mp-weixin"].options);
            var p = {
              options: l,
              data: _t(u, d.default.prototype),
              behaviors: vt(u, ze),
              properties: bt(u.props, !1, u.__file, l),
              lifetimes: {
                attached: function () {
                  var e = this.properties,
                    t = {
                      mpType: n.call(this) ? "page" : "component",
                      mpInstance: this,
                      propsData: e,
                    };
                  ft(e.vueId, this),
                    r.call(this, { vuePid: this._$vuePid, vueOptions: t }),
                    (this.$vm = new c(t)),
                    dt(this.$vm, e.vueSlots),
                    this.$vm.$mount();
                },
                ready: function () {
                  this.$vm &&
                    ((this.$vm._isMounted = !0),
                    this.$vm.__call_hook("mounted"),
                    this.$vm.__call_hook("onReady"));
                },
                detached: function () {
                  this.$vm && this.$vm.$destroy();
                },
              },
              pageLifetimes: {
                show: function (e) {
                  this.$vm && this.$vm.__call_hook("onPageShow", e);
                },
                hide: function () {
                  this.$vm && this.$vm.__call_hook("onPageHide");
                },
                resize: function (e) {
                  this.$vm && this.$vm.__call_hook("onPageResize", e);
                },
              },
              methods: { __l: Ze, __e: St },
            };
            return (
              u.externalClasses && (p.externalClasses = u.externalClasses),
              Array.isArray(u.wxsCallMethods) &&
                u.wxsCallMethods.forEach(function (e) {
                  p.methods[e] = function (t) {
                    return this.$vm[e](t);
                  };
                }),
              a ? [p, u, c] : n ? p : [p, c]
            );
          })(e, { isPage: Je, initRelation: We }, t);
        }
        var Mt = ["onShow", "onHide", "onUnload"];
        function Bt(e) {
          var t,
            n,
            r = Dt(e, !0),
            a = (0, o.default)(r, 2),
            i = a[0],
            s = a[1];
          return (
            st(i.methods, Mt, s),
            (i.methods.onLoad = function (e) {
              this.options = e;
              var t = Object.assign({}, e);
              delete t.__id__,
                (this.$page = {
                  fullPath: "/" + (this.route || this.is) + Lt(t),
                }),
                (this.$vm.$mp.query = e),
                this.$vm.__call_hook("onLoad", e);
            }),
            ct(i.methods, e, ["onReady"]),
            (t = i.methods),
            (n = s.methods) &&
              Object.keys(n).forEach(function (e) {
                var r = e.match(et);
                if (r) {
                  var a = r[1];
                  (t[e] = n[e]), (t[a] = n[a]);
                }
              }),
            i
          );
        }
        function Ft(e) {
          return Component(
            (function (e) {
              return Bt(e);
            })(e)
          );
        }
        function Nt(e) {
          return Component(Dt(e));
        }
        function Vt(t) {
          var n = Et(t),
            r = getApp({ allowDefault: !0 });
          t.$scope = r;
          var a = r.globalData;
          if (
            (a &&
              Object.keys(n.globalData).forEach(function (e) {
                S(a, e) || (a[e] = n.globalData[e]);
              }),
            Object.keys(n).forEach(function (e) {
              S(r, e) || (r[e] = n[e]);
            }),
            y(n.onShow) &&
              e.onAppShow &&
              e.onAppShow(function () {
                for (
                  var e = arguments.length, n = new Array(e), r = 0;
                  r < e;
                  r++
                )
                  n[r] = arguments[r];
                t.__call_hook("onShow", n);
              }),
            y(n.onHide) &&
              e.onAppHide &&
              e.onAppHide(function () {
                for (
                  var e = arguments.length, n = new Array(e), r = 0;
                  r < e;
                  r++
                )
                  n[r] = arguments[r];
                t.__call_hook("onHide", n);
              }),
            y(n.onLaunch))
          ) {
            var i = e.getLaunchOptionsSync && e.getLaunchOptionsSync();
            t.__call_hook("onLaunch", i);
          }
          return t;
        }
        function Rt(t) {
          var n = Et(t);
          if (
            (y(n.onShow) &&
              e.onAppShow &&
              e.onAppShow(function () {
                for (
                  var e = arguments.length, n = new Array(e), r = 0;
                  r < e;
                  r++
                )
                  n[r] = arguments[r];
                t.__call_hook("onShow", n);
              }),
            y(n.onHide) &&
              e.onAppHide &&
              e.onAppHide(function () {
                for (
                  var e = arguments.length, n = new Array(e), r = 0;
                  r < e;
                  r++
                )
                  n[r] = arguments[r];
                t.__call_hook("onHide", n);
              }),
            y(n.onLaunch))
          ) {
            var r = e.getLaunchOptionsSync && e.getLaunchOptionsSync();
            t.__call_hook("onLaunch", r);
          }
          return t;
        }
        Mt.push.apply(Mt, [
          "onPullDownRefresh",
          "onReachBottom",
          "onAddToFavorites",
          "onShareTimeline",
          "onShareAppMessage",
          "onPageScroll",
          "onResize",
          "onTabItemTap",
        ]),
          ["vibrate", "preloadPage", "unPreloadPage", "loadSubPackage"].forEach(
            function (e) {
              xe[e] = !1;
            }
          ),
          [].forEach(function (t) {
            var n = xe[t] && xe[t].name ? xe[t].name : t;
            e.canIUse(n) || (xe[t] = !1);
          });
        var Ut = {};
        "undefined" != typeof Proxy
          ? (Ut = new Proxy(
              {},
              {
                get: function (t, n) {
                  return S(t, n)
                    ? t[n]
                    : pe[n]
                    ? pe[n]
                    : qe[n]
                    ? Z(n, qe[n])
                    : Pe[n]
                    ? Z(n, Pe[n])
                    : $e[n]
                    ? Z(n, $e[n])
                    : Me[n]
                    ? Me[n]
                    : Z(n, ke(n, e[n]));
                },
                set: function (e, t, n) {
                  return (e[t] = n), !0;
                },
              }
            ))
          : (Object.keys(pe).forEach(function (e) {
              Ut[e] = pe[e];
            }),
            Object.keys($e).forEach(function (e) {
              Ut[e] = Z(e, $e[e]);
            }),
            Object.keys(Pe).forEach(function (e) {
              Ut[e] = Z(e, Pe[e]);
            }),
            Object.keys(Me).forEach(function (e) {
              Ut[e] = Me[e];
            }),
            Object.keys(qe).forEach(function (e) {
              Ut[e] = Z(e, qe[e]);
            }),
            Object.keys(e).forEach(function (t) {
              (S(e, t) || S(xe, t)) && (Ut[t] = Z(t, ke(t, e[t])));
            })),
          (e.createApp = jt),
          (e.createPage = Ft),
          (e.createComponent = Nt),
          (e.createSubpackageApp = Vt),
          (e.createPlugin = Rt);
        var Ht = Ut;
        t.default = Ht;
      }).call(
        this,
        n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1).default,
        n(/*! ./../../../webpack/buildin/global.js */ 3)
      );
    },
    /*!***********************************!*\
  !*** (webpack)/buildin/global.js ***!
  \***********************************/
    /*! no static exports found */ function (t, n) {
      var r;
      r = (function () {
        return this;
      })();
      try {
        r = r || new Function("return this")();
      } catch (t) {
        "object" === ("undefined" == typeof window ? "undefined" : e(window)) &&
          (r = window);
      }
      t.exports = r;
    },
    /*!**********************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/interopRequireDefault.js ***!
  \**********************************************************************/
    /*! no static exports found */ function (e, t) {
      (e.exports = function (e) {
        return e && e.__esModule ? e : { default: e };
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!**************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/slicedToArray.js ***!
  \**************************************************************/
    /*! no static exports found */ function (e, t, n) {
      var r = n(/*! ./arrayWithHoles.js */ 6),
        a = n(/*! ./iterableToArrayLimit.js */ 7),
        i = n(/*! ./unsupportedIterableToArray.js */ 8),
        o = n(/*! ./nonIterableRest.js */ 10);
      (e.exports = function (e, t) {
        return r(e) || a(e, t) || i(e, t) || o();
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!***************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/arrayWithHoles.js ***!
  \***************************************************************/
    /*! no static exports found */ function (e, t) {
      (e.exports = function (e) {
        if (Array.isArray(e)) return e;
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!*********************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/iterableToArrayLimit.js ***!
  \*********************************************************************/
    /*! no static exports found */ function (e, t) {
      (e.exports = function (e, t) {
        var n =
          null == e
            ? null
            : ("undefined" != typeof Symbol && e[Symbol.iterator]) ||
              e["@@iterator"];
        if (null != n) {
          var r,
            a,
            i,
            o,
            s = [],
            c = !0,
            u = !1;
          try {
            if (((i = (n = n.call(e)).next), 0 === t)) {
              if (Object(n) !== n) return;
              c = !1;
            } else
              for (
                ;
                !(c = (r = i.call(n)).done) &&
                (s.push(r.value), s.length !== t);
                c = !0
              );
          } catch (e) {
            (u = !0), (a = e);
          } finally {
            try {
              if (!c && null != n.return && ((o = n.return()), Object(o) !== o))
                return;
            } finally {
              if (u) throw a;
            }
          }
          return s;
        }
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!***************************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/unsupportedIterableToArray.js ***!
  \***************************************************************************/
    /*! no static exports found */ function (e, t, n) {
      var r = n(/*! ./arrayLikeToArray.js */ 9);
      (e.exports = function (e, t) {
        if (e) {
          if ("string" == typeof e) return r(e, t);
          var n = Object.prototype.toString.call(e).slice(8, -1);
          return (
            "Object" === n && e.constructor && (n = e.constructor.name),
            "Map" === n || "Set" === n
              ? Array.from(e)
              : "Arguments" === n ||
                /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)
              ? r(e, t)
              : void 0
          );
        }
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!*****************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/arrayLikeToArray.js ***!
  \*****************************************************************/
    /*! no static exports found */ function (e, t) {
      (e.exports = function (e, t) {
        (null == t || t > e.length) && (t = e.length);
        for (var n = 0, r = new Array(t); n < t; n++) r[n] = e[n];
        return r;
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!****************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/nonIterableRest.js ***!
  \****************************************************************/
    /*! no static exports found */ function (e, t) {
      (e.exports = function () {
        throw new TypeError(
          "Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."
        );
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!***************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/defineProperty.js ***!
  \***************************************************************/
    /*! no static exports found */ function (e, t, n) {
      var r = n(/*! ./toPropertyKey.js */ 12);
      (e.exports = function (e, t, n) {
        return (
          (t = r(t)) in e
            ? Object.defineProperty(e, t, {
                value: n,
                enumerable: !0,
                configurable: !0,
                writable: !0,
              })
            : (e[t] = n),
          e
        );
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!**************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/toPropertyKey.js ***!
  \**************************************************************/
    /*! no static exports found */ function (e, t, n) {
      var r = n(/*! ./typeof.js */ 13).default,
        a = n(/*! ./toPrimitive.js */ 14);
      (e.exports = function (e) {
        var t = a(e, "string");
        return "symbol" == r(t) ? t : t + "";
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!*******************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/typeof.js ***!
  \*******************************************************/
    /*! no static exports found */ function (e, t) {
      function n(t) {
        return (
          (e.exports = n =
            "function" == typeof Symbol && "symbol" == typeof Symbol.iterator
              ? function (e) {
                  return typeof e;
                }
              : function (e) {
                  return e &&
                    "function" == typeof Symbol &&
                    e.constructor === Symbol &&
                    e !== Symbol.prototype
                    ? "symbol"
                    : typeof e;
                }),
          (e.exports.__esModule = !0),
          (e.exports.default = e.exports),
          n(t)
        );
      }
      (e.exports = n),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/toPrimitive.js ***!
  \************************************************************/
    /*! no static exports found */ function (e, t, n) {
      var r = n(/*! ./typeof.js */ 13).default;
      (e.exports = function (e, t) {
        if ("object" != r(e) || !e) return e;
        var n = e[Symbol.toPrimitive];
        if (void 0 !== n) {
          var a = n.call(e, t || "default");
          if ("object" != r(a)) return a;
          throw new TypeError("@@toPrimitive must return a primitive value.");
        }
        return ("string" === t ? String : Number)(e);
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!**********************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/construct.js ***!
  \**********************************************************/
    /*! no static exports found */ function (e, t, n) {
      var r = n(/*! ./setPrototypeOf.js */ 16),
        a = n(/*! ./isNativeReflectConstruct.js */ 17);
      (e.exports = function (e, t, n) {
        if (a()) return Reflect.construct.apply(null, arguments);
        var i = [null];
        i.push.apply(i, t);
        var o = new (e.bind.apply(e, i))();
        return n && r(o, n.prototype), o;
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!***************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/setPrototypeOf.js ***!
  \***************************************************************/
    /*! no static exports found */ function (e, t) {
      function n(t, r) {
        return (
          (e.exports = n =
            Object.setPrototypeOf
              ? Object.setPrototypeOf.bind()
              : function (e, t) {
                  return (e.__proto__ = t), e;
                }),
          (e.exports.__esModule = !0),
          (e.exports.default = e.exports),
          n(t, r)
        );
      }
      (e.exports = n),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!*************************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/isNativeReflectConstruct.js ***!
  \*************************************************************************/
    /*! no static exports found */ function (e, t) {
      function n() {
        try {
          var t = !Boolean.prototype.valueOf.call(
            Reflect.construct(Boolean, [], function () {})
          );
        } catch (t) {}
        return ((e.exports = n =
          function () {
            return !!t;
          }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports))();
      }
      (e.exports = n),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!******************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/toConsumableArray.js ***!
  \******************************************************************/
    /*! no static exports found */ function (e, t, n) {
      var r = n(/*! ./arrayWithoutHoles.js */ 19),
        a = n(/*! ./iterableToArray.js */ 20),
        i = n(/*! ./unsupportedIterableToArray.js */ 8),
        o = n(/*! ./nonIterableSpread.js */ 21);
      (e.exports = function (e) {
        return r(e) || a(e) || i(e) || o();
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!******************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/arrayWithoutHoles.js ***!
  \******************************************************************/
    /*! no static exports found */ function (e, t, n) {
      var r = n(/*! ./arrayLikeToArray.js */ 9);
      (e.exports = function (e) {
        if (Array.isArray(e)) return r(e);
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!****************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/iterableToArray.js ***!
  \****************************************************************/
    /*! no static exports found */ function (e, t) {
      (e.exports = function (e) {
        if (
          ("undefined" != typeof Symbol && null != e[Symbol.iterator]) ||
          null != e["@@iterator"]
        )
          return Array.from(e);
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!******************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/nonIterableSpread.js ***!
  \******************************************************************/
    /*! no static exports found */ function (e, t) {
      (e.exports = function () {
        throw new TypeError(
          "Invalid attempt to spread non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."
        );
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!*************************************************************!*\
  !*** ./node_modules/@dcloudio/uni-i18n/dist/uni-i18n.es.js ***!
  \*************************************************************/
    /*! no static exports found */ function (e, t, n) {
      (function (e, r) {
        var a = n(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
        Object.defineProperty(t, "__esModule", { value: !0 }),
          (t.LOCALE_ZH_HANT =
            t.LOCALE_ZH_HANS =
            t.LOCALE_FR =
            t.LOCALE_ES =
            t.LOCALE_EN =
            t.I18n =
            t.Formatter =
              void 0),
          (t.compileI18nJsonStr = function (e, t) {
            var n = t.locale,
              r = t.locales,
              a = t.delimiters;
            if (!A(e, a)) return e;
            S || (S = new p());
            var i = [];
            Object.keys(r).forEach(function (e) {
              e !== n && i.push({ locale: e, values: r[e] });
            }),
              i.unshift({ locale: n, values: r[n] });
            try {
              return JSON.stringify($(JSON.parse(e), i, a), null, 2);
            } catch (e) {}
            return e;
          }),
          (t.hasI18nJson = function e(t, n) {
            S || (S = new p());
            return E(t, function (t, r) {
              var a = t[r];
              return O(a) ? !!A(a, n) || void 0 : e(a, n);
            });
          }),
          (t.initVueI18n = function (e) {
            var t =
                arguments.length > 1 && void 0 !== arguments[1]
                  ? arguments[1]
                  : {},
              n = arguments.length > 2 ? arguments[2] : void 0,
              r = arguments.length > 3 ? arguments[3] : void 0;
            if ("string" != typeof e) {
              var a = [t, e];
              (e = a[0]), (t = a[1]);
            }
            "string" != typeof e && (e = w());
            "string" != typeof n &&
              (n =
                ("undefined" != typeof __uniConfig &&
                  __uniConfig.fallbackLocale) ||
                "en");
            var i = new y({
                locale: e,
                fallbackLocale: n,
                messages: t,
                watcher: r,
              }),
              o = function (e, t) {
                if ("function" != typeof getApp)
                  o = function (e, t) {
                    return i.t(e, t);
                  };
                else {
                  var n = !1;
                  o = function (e, t) {
                    var r = getApp().$vm;
                    return (
                      r && (r.$locale, n || ((n = !0), x(r, i))), i.t(e, t)
                    );
                  };
                }
                return o(e, t);
              };
            return {
              i18n: i,
              f: function (e, t, n) {
                return i.f(e, t, n);
              },
              t: function (e, t) {
                return o(e, t);
              },
              add: function (e, t) {
                var n =
                  !(arguments.length > 2 && void 0 !== arguments[2]) ||
                  arguments[2];
                return i.add(e, t, n);
              },
              watch: function (e) {
                return i.watchLocale(e);
              },
              getLocale: function () {
                return i.getLocale();
              },
              setLocale: function (e) {
                return i.setLocale(e);
              },
            };
          }),
          (t.isI18nStr = A),
          (t.isString = void 0),
          (t.normalizeLocale = b),
          (t.parseI18nJson = function e(t, n, r) {
            S || (S = new p());
            return (
              E(t, function (t, a) {
                var i = t[a];
                O(i) ? A(i, r) && (t[a] = k(i, n, r)) : e(i, n, r);
              }),
              t
            );
          }),
          (t.resolveLocale = function (e) {
            return function (t) {
              return t
                ? (function (e) {
                    var t = [],
                      n = e.split("-");
                    for (; n.length; ) t.push(n.join("-")), n.pop();
                    return t;
                  })((t = b(t) || t)).find(function (t) {
                    return e.indexOf(t) > -1;
                  })
                : t;
            };
          });
        var i = a(n(/*! @babel/runtime/helpers/slicedToArray */ 5)),
          o = a(n(/*! @babel/runtime/helpers/classCallCheck */ 23)),
          s = a(n(/*! @babel/runtime/helpers/createClass */ 24)),
          c = a(n(/*! @babel/runtime/helpers/typeof */ 13)),
          u = function (e) {
            return null !== e && "object" === (0, c.default)(e);
          },
          l = ["{", "}"],
          p = (function () {
            function e() {
              (0, o.default)(this, e), (this._caches = Object.create(null));
            }
            return (
              (0, s.default)(e, [
                {
                  key: "interpolate",
                  value: function (e, t) {
                    var n =
                      arguments.length > 2 && void 0 !== arguments[2]
                        ? arguments[2]
                        : l;
                    if (!t) return [e];
                    var r = this._caches[e];
                    return r || ((r = _(e, n)), (this._caches[e] = r)), h(r, t);
                  },
                },
              ]),
              e
            );
          })();
        t.Formatter = p;
        var d = /^(?:\d)+/,
          f = /^(?:\w)+/;
        function _(e, t) {
          for (
            var n = (0, i.default)(t, 2),
              r = n[0],
              a = n[1],
              o = [],
              s = 0,
              c = "";
            s < e.length;

          ) {
            var u = e[s++];
            if (u === r) {
              c && o.push({ type: "text", value: c }), (c = "");
              var l = "";
              for (u = e[s++]; void 0 !== u && u !== a; )
                (l += u), (u = e[s++]);
              var p = u === a,
                _ = d.test(l) ? "list" : p && f.test(l) ? "named" : "unknown";
              o.push({ value: l, type: _ });
            } else c += u;
          }
          return c && o.push({ type: "text", value: c }), o;
        }
        function h(e, t) {
          var n = [],
            r = 0,
            a = Array.isArray(t) ? "list" : u(t) ? "named" : "unknown";
          if ("unknown" === a) return n;
          for (; r < e.length; ) {
            var i = e[r];
            switch (i.type) {
              case "text":
                n.push(i.value);
                break;
              case "list":
                n.push(t[parseInt(i.value, 10)]);
                break;
              case "named":
                "named" === a
                  ? n.push(t[i.value])
                  : console.warn(
                      "Type of token '"
                        .concat(i.type, "' and format of value '")
                        .concat(a, "' don't match!")
                    );
                break;
              case "unknown":
                console.warn("Detect 'unknown' type of token!");
            }
            r++;
          }
          return n;
        }
        t.LOCALE_ZH_HANS = "zh-Hans";
        t.LOCALE_ZH_HANT = "zh-Hant";
        t.LOCALE_EN = "en";
        t.LOCALE_FR = "fr";
        t.LOCALE_ES = "es";
        var m = Object.prototype.hasOwnProperty,
          v = function (e, t) {
            return m.call(e, t);
          },
          g = new p();
        function b(e, t) {
          if (e) {
            if (((e = e.trim().replace(/_/g, "-")), t && t[e])) return e;
            if ("chinese" === (e = e.toLowerCase())) return "zh-Hans";
            if (0 === e.indexOf("zh"))
              return e.indexOf("-hans") > -1
                ? "zh-Hans"
                : e.indexOf("-hant") > -1
                ? "zh-Hant"
                : ((n = e),
                  ["-tw", "-hk", "-mo", "-cht"].find(function (e) {
                    return -1 !== n.indexOf(e);
                  })
                    ? "zh-Hant"
                    : "zh-Hans");
            var n,
              r = ["en", "fr", "es"];
            t && Object.keys(t).length > 0 && (r = Object.keys(t));
            var a = (function (e, t) {
              return t.find(function (t) {
                return 0 === e.indexOf(t);
              });
            })(e, r);
            return a || void 0;
          }
        }
        var y = (function () {
          function e(t) {
            var n = t.locale,
              r = t.fallbackLocale,
              a = t.messages,
              i = t.watcher,
              s = t.formater;
            (0, o.default)(this, e),
              (this.locale = "en"),
              (this.fallbackLocale = "en"),
              (this.message = {}),
              (this.messages = {}),
              (this.watchers = []),
              r && (this.fallbackLocale = r),
              (this.formater = s || g),
              (this.messages = a || {}),
              this.setLocale(n || "en"),
              i && this.watchLocale(i);
          }
          return (
            (0, s.default)(e, [
              {
                key: "setLocale",
                value: function (e) {
                  var t = this,
                    n = this.locale;
                  (this.locale = b(e, this.messages) || this.fallbackLocale),
                    this.messages[this.locale] ||
                      (this.messages[this.locale] = {}),
                    (this.message = this.messages[this.locale]),
                    n !== this.locale &&
                      this.watchers.forEach(function (e) {
                        e(t.locale, n);
                      });
                },
              },
              {
                key: "getLocale",
                value: function () {
                  return this.locale;
                },
              },
              {
                key: "watchLocale",
                value: function (e) {
                  var t = this,
                    n = this.watchers.push(e) - 1;
                  return function () {
                    t.watchers.splice(n, 1);
                  };
                },
              },
              {
                key: "add",
                value: function (e, t) {
                  var n =
                      !(arguments.length > 2 && void 0 !== arguments[2]) ||
                      arguments[2],
                    r = this.messages[e];
                  r
                    ? n
                      ? Object.assign(r, t)
                      : Object.keys(t).forEach(function (e) {
                          v(r, e) || (r[e] = t[e]);
                        })
                    : (this.messages[e] = t);
                },
              },
              {
                key: "f",
                value: function (e, t, n) {
                  return this.formater.interpolate(e, t, n).join("");
                },
              },
              {
                key: "t",
                value: function (e, t, n) {
                  var r = this.message;
                  return (
                    "string" == typeof t
                      ? (t = b(t, this.messages)) && (r = this.messages[t])
                      : (n = t),
                    v(r, e)
                      ? this.formater.interpolate(r[e], n).join("")
                      : (console.warn(
                          "Cannot translate the value of keypath ".concat(
                            e,
                            ". Use the value of keypath as default."
                          )
                        ),
                        e)
                  );
                },
              },
            ]),
            e
          );
        })();
        function x(e, t) {
          e.$watchLocale
            ? e.$watchLocale(function (e) {
                t.setLocale(e);
              })
            : e.$watch(
                function () {
                  return e.$locale;
                },
                function (e) {
                  t.setLocale(e);
                }
              );
        }
        function w() {
          return void 0 !== e && e.getLocale
            ? e.getLocale()
            : void 0 !== r && r.getLocale
            ? r.getLocale()
            : "en";
        }
        t.I18n = y;
        var S,
          O = function (e) {
            return "string" == typeof e;
          };
        function A(e, t) {
          return e.indexOf(t[0]) > -1;
        }
        function k(e, t, n) {
          return S.interpolate(e, t, n).join("");
        }
        function $(e, t, n) {
          return (
            E(e, function (e, r) {
              !(function (e, t, n, r) {
                var a = e[t];
                if (O(a)) {
                  if (
                    A(a, r) &&
                    ((e[t] = k(a, n[0].values, r)), n.length > 1)
                  ) {
                    var i = (e[t + "Locales"] = {});
                    n.forEach(function (e) {
                      i[e.locale] = k(a, e.values, r);
                    });
                  }
                } else $(a, n, r);
              })(e, r, t, n);
            }),
            e
          );
        }
        function E(e, t) {
          if (Array.isArray(e)) {
            for (var n = 0; n < e.length; n++) if (t(e, n)) return !0;
          } else if (u(e)) for (var r in e) if (t(e, r)) return !0;
          return !1;
        }
        t.isString = O;
      }).call(
        this,
        n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2)
          .default,
        n(/*! ./../../../webpack/buildin/global.js */ 3)
      );
    },
    /*!***************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/classCallCheck.js ***!
  \***************************************************************/
    /*! no static exports found */ function (e, t) {
      (e.exports = function (e, t) {
        if (!(e instanceof t))
          throw new TypeError("Cannot call a class as a function");
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/createClass.js ***!
  \************************************************************/
    /*! no static exports found */ function (e, t, n) {
      var r = n(/*! ./toPropertyKey.js */ 12);
      function a(e, t) {
        for (var n = 0; n < t.length; n++) {
          var a = t[n];
          (a.enumerable = a.enumerable || !1),
            (a.configurable = !0),
            "value" in a && (a.writable = !0),
            Object.defineProperty(e, r(a.key), a);
        }
      }
      (e.exports = function (e, t, n) {
        return (
          t && a(e.prototype, t),
          n && a(e, n),
          Object.defineProperty(e, "prototype", { writable: !1 }),
          e
        );
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!******************************************************************************************!*\
  !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/mp-vue/dist/mp.runtime.esm.js ***!
  \******************************************************************************************/
    /*! exports provided: default */ function (t, n, r) {
      r.r(n),
        function (t) {
          /*!
           * Vue.js v2.6.11
           * (c) 2014-2024 Evan You
           * Released under the MIT License.
           */
          var r = Object.freeze({});
          function a(e) {
            return null == e;
          }
          function i(e) {
            return null != e;
          }
          function o(e) {
            return !0 === e;
          }
          function s(t) {
            return (
              "string" == typeof t ||
              "number" == typeof t ||
              "symbol" === e(t) ||
              "boolean" == typeof t
            );
          }
          function c(t) {
            return null !== t && "object" === e(t);
          }
          var u = Object.prototype.toString;
          function l(e) {
            return u.call(e).slice(8, -1);
          }
          function p(e) {
            return "[object Object]" === u.call(e);
          }
          function d(e) {
            var t = parseFloat(String(e));
            return t >= 0 && Math.floor(t) === t && isFinite(e);
          }
          function f(e) {
            return (
              i(e) &&
              "function" == typeof e.then &&
              "function" == typeof e.catch
            );
          }
          function _(e) {
            return null == e
              ? ""
              : Array.isArray(e) || (p(e) && e.toString === u)
              ? JSON.stringify(e, null, 2)
              : String(e);
          }
          function h(e) {
            var t = parseFloat(e);
            return isNaN(t) ? e : t;
          }
          function m(e, t) {
            for (
              var n = Object.create(null), r = e.split(","), a = 0;
              a < r.length;
              a++
            )
              n[r[a]] = !0;
            return t
              ? function (e) {
                  return n[e.toLowerCase()];
                }
              : function (e) {
                  return n[e];
                };
          }
          var v = m("slot,component", !0),
            g = m("key,ref,slot,slot-scope,is");
          function b(e, t) {
            if (e.length) {
              var n = e.indexOf(t);
              if (n > -1) return e.splice(n, 1);
            }
          }
          var y = Object.prototype.hasOwnProperty;
          function x(e, t) {
            return y.call(e, t);
          }
          function w(e) {
            var t = Object.create(null);
            return function (n) {
              return t[n] || (t[n] = e(n));
            };
          }
          var S = /-(\w)/g,
            O = w(function (e) {
              return e.replace(S, function (e, t) {
                return t ? t.toUpperCase() : "";
              });
            }),
            A = w(function (e) {
              return e.charAt(0).toUpperCase() + e.slice(1);
            }),
            k = /\B([A-Z])/g,
            $ = w(function (e) {
              return e.replace(k, "-$1").toLowerCase();
            });
          var E = Function.prototype.bind
            ? function (e, t) {
                return e.bind(t);
              }
            : function (e, t) {
                function n(n) {
                  var r = arguments.length;
                  return r
                    ? r > 1
                      ? e.apply(t, arguments)
                      : e.call(t, n)
                    : e.call(t);
                }
                return (n._length = e.length), n;
              };
          function j(e, t) {
            t = t || 0;
            for (var n = e.length - t, r = new Array(n); n--; ) r[n] = e[n + t];
            return r;
          }
          function P(e, t) {
            for (var n in t) e[n] = t[n];
            return e;
          }
          function C(e) {
            for (var t = {}, n = 0; n < e.length; n++) e[n] && P(t, e[n]);
            return t;
          }
          function I(e, t, n) {}
          var T = function (e, t, n) {
              return !1;
            },
            L = function (e) {
              return e;
            };
          function D(e, t) {
            if (e === t) return !0;
            var n = c(e),
              r = c(t);
            if (!n || !r) return !n && !r && String(e) === String(t);
            try {
              var a = Array.isArray(e),
                i = Array.isArray(t);
              if (a && i)
                return (
                  e.length === t.length &&
                  e.every(function (e, n) {
                    return D(e, t[n]);
                  })
                );
              if (e instanceof Date && t instanceof Date)
                return e.getTime() === t.getTime();
              if (a || i) return !1;
              var o = Object.keys(e),
                s = Object.keys(t);
              return (
                o.length === s.length &&
                o.every(function (n) {
                  return D(e[n], t[n]);
                })
              );
            } catch (e) {
              return !1;
            }
          }
          function M(e, t) {
            for (var n = 0; n < e.length; n++) if (D(e[n], t)) return n;
            return -1;
          }
          function B(e) {
            var t = !1;
            return function () {
              t || ((t = !0), e.apply(this, arguments));
            };
          }
          var F = ["component", "directive", "filter"],
            N = [
              "beforeCreate",
              "created",
              "beforeMount",
              "mounted",
              "beforeUpdate",
              "updated",
              "beforeDestroy",
              "destroyed",
              "activated",
              "deactivated",
              "errorCaptured",
              "serverPrefetch",
            ],
            V = {
              optionMergeStrategies: Object.create(null),
              silent: !1,
              productionTip: !0,
              devtools: !0,
              performance: !1,
              errorHandler: null,
              warnHandler: null,
              ignoredElements: [],
              keyCodes: Object.create(null),
              isReservedTag: T,
              isReservedAttr: T,
              isUnknownElement: T,
              getTagNamespace: I,
              parsePlatformTagName: L,
              mustUseProp: T,
              async: !0,
              _lifecycleHooks: N,
            },
            R =
              /a-zA-Z\u00B7\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u037D\u037F-\u1FFF\u200C-\u200D\u203F-\u2040\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD/;
          function U(e) {
            var t = (e + "").charCodeAt(0);
            return 36 === t || 95 === t;
          }
          function H(e, t, n, r) {
            Object.defineProperty(e, t, {
              value: n,
              enumerable: !!r,
              writable: !0,
              configurable: !0,
            });
          }
          var K = new RegExp("[^" + R.source + ".$_\\d]");
          var q,
            G = "__proto__" in {},
            z = "undefined" != typeof window,
            J = "undefined" != typeof WXEnvironment && !!WXEnvironment.platform,
            W = J && WXEnvironment.platform.toLowerCase(),
            X =
              z && window.navigator && window.navigator.userAgent.toLowerCase(),
            Z = X && /msie|trident/.test(X),
            Y = (X && X.indexOf("msie 9.0"), X && X.indexOf("edge/") > 0),
            Q =
              (X && X.indexOf("android"),
              (X && /iphone|ipad|ipod|ios/.test(X)) || "ios" === W),
            ee =
              (X && /chrome\/\d+/.test(X),
              X && /phantomjs/.test(X),
              X && X.match(/firefox\/(\d+)/),
              {}.watch);
          if (z)
            try {
              var te = {};
              Object.defineProperty(te, "passive", { get: function () {} }),
                window.addEventListener("test-passive", null, te);
            } catch (e) {}
          var ne = function () {
              return (
                void 0 === q &&
                  (q =
                    !z &&
                    !J &&
                    void 0 !== t &&
                    t.process &&
                    "server" === t.process.env.VUE_ENV),
                q
              );
            },
            re = z && window.__VUE_DEVTOOLS_GLOBAL_HOOK__;
          function ae(e) {
            return "function" == typeof e && /native code/.test(e.toString());
          }
          var ie,
            oe =
              "undefined" != typeof Symbol &&
              ae(Symbol) &&
              "undefined" != typeof Reflect &&
              ae(Reflect.ownKeys);
          ie =
            "undefined" != typeof Set && ae(Set)
              ? Set
              : (function () {
                  function e() {
                    this.set = Object.create(null);
                  }
                  return (
                    (e.prototype.has = function (e) {
                      return !0 === this.set[e];
                    }),
                    (e.prototype.add = function (e) {
                      this.set[e] = !0;
                    }),
                    (e.prototype.clear = function () {
                      this.set = Object.create(null);
                    }),
                    e
                  );
                })();
          var se = I,
            ce = I,
            ue = I,
            le = I,
            pe = "undefined" != typeof console,
            de = /(?:^|[-_])(\w)/g;
          (se = function (e, t) {
            var n = t ? ue(t) : "";
            V.warnHandler
              ? V.warnHandler.call(null, e, t, n)
              : pe && !V.silent && console.error("[Vue warn]: " + e + n);
          }),
            (ce = function (e, t) {
              pe &&
                !V.silent &&
                console.warn("[Vue tip]: " + e + (t ? ue(t) : ""));
            }),
            (le = function (e, t) {
              if (e.$root === e)
                return e.$options && e.$options.__file
                  ? "" + e.$options.__file
                  : "<Root>";
              var n =
                  "function" == typeof e && null != e.cid
                    ? e.options
                    : e._isVue
                    ? e.$options || e.constructor.options
                    : e,
                r = n.name || n._componentTag,
                a = n.__file;
              if (!r && a) {
                var i = a.match(/([^/\\]+)\.vue$/);
                r = i && i[1];
              }
              return (
                (r
                  ? "<" +
                    (r
                      .replace(de, function (e) {
                        return e.toUpperCase();
                      })
                      .replace(/[-_]/g, "") +
                      ">")
                  : "<Anonymous>") + (a && !1 !== t ? " at " + a : "")
              );
            });
          ue = function (e) {
            if (e._isVue && e.$parent) {
              for (var t = [], n = 0; e && "PageBody" !== e.$options.name; ) {
                if (t.length > 0) {
                  var r = t[t.length - 1];
                  if (r.constructor === e.constructor) {
                    n++, (e = e.$parent);
                    continue;
                  }
                  n > 0 && ((t[t.length - 1] = [r, n]), (n = 0));
                }
                !e.$options.isReserved && t.push(e), (e = e.$parent);
              }
              return (
                "\n\nfound in\n\n" +
                t
                  .map(function (e, t) {
                    return (
                      "" +
                      (0 === t
                        ? "---\x3e "
                        : (function (e, t) {
                            for (var n = ""; t; )
                              t % 2 == 1 && (n += e),
                                t > 1 && (e += e),
                                (t >>= 1);
                            return n;
                          })(" ", 5 + 2 * t)) +
                      (Array.isArray(e)
                        ? le(e[0]) + "... (" + e[1] + " recursive calls)"
                        : le(e))
                    );
                  })
                  .join("\n")
              );
            }
            return "\n\n(found in " + le(e) + ")";
          };
          var fe = 0,
            _e = function () {
              (this.id = fe++), (this.subs = []);
            };
          function he(e) {
            _e.SharedObject.targetStack.push(e),
              (_e.SharedObject.target = e),
              (_e.target = e);
          }
          function me() {
            _e.SharedObject.targetStack.pop(),
              (_e.SharedObject.target =
                _e.SharedObject.targetStack[
                  _e.SharedObject.targetStack.length - 1
                ]),
              (_e.target = _e.SharedObject.target);
          }
          (_e.prototype.addSub = function (e) {
            this.subs.push(e);
          }),
            (_e.prototype.removeSub = function (e) {
              b(this.subs, e);
            }),
            (_e.prototype.depend = function () {
              _e.SharedObject.target && _e.SharedObject.target.addDep(this);
            }),
            (_e.prototype.notify = function () {
              var e = this.subs.slice();
              V.async ||
                e.sort(function (e, t) {
                  return e.id - t.id;
                });
              for (var t = 0, n = e.length; t < n; t++) e[t].update();
            }),
            ((_e.SharedObject = {}).target = null),
            (_e.SharedObject.targetStack = []);
          var ve = function (e, t, n, r, a, i, o, s) {
              (this.tag = e),
                (this.data = t),
                (this.children = n),
                (this.text = r),
                (this.elm = a),
                (this.ns = void 0),
                (this.context = i),
                (this.fnContext = void 0),
                (this.fnOptions = void 0),
                (this.fnScopeId = void 0),
                (this.key = t && t.key),
                (this.componentOptions = o),
                (this.componentInstance = void 0),
                (this.parent = void 0),
                (this.raw = !1),
                (this.isStatic = !1),
                (this.isRootInsert = !0),
                (this.isComment = !1),
                (this.isCloned = !1),
                (this.isOnce = !1),
                (this.asyncFactory = s),
                (this.asyncMeta = void 0),
                (this.isAsyncPlaceholder = !1);
            },
            ge = { child: { configurable: !0 } };
          (ge.child.get = function () {
            return this.componentInstance;
          }),
            Object.defineProperties(ve.prototype, ge);
          var be = function (e) {
            void 0 === e && (e = "");
            var t = new ve();
            return (t.text = e), (t.isComment = !0), t;
          };
          function ye(e) {
            return new ve(void 0, void 0, void 0, String(e));
          }
          var xe = Array.prototype,
            we = Object.create(xe);
          [
            "push",
            "pop",
            "shift",
            "unshift",
            "splice",
            "sort",
            "reverse",
          ].forEach(function (e) {
            var t = xe[e];
            H(we, e, function () {
              for (var n = [], r = arguments.length; r--; ) n[r] = arguments[r];
              var a,
                i = t.apply(this, n),
                o = this.__ob__;
              switch (e) {
                case "push":
                case "unshift":
                  a = n;
                  break;
                case "splice":
                  a = n.slice(2);
              }
              return a && o.observeArray(a), o.dep.notify(), i;
            });
          });
          var Se = Object.getOwnPropertyNames(we),
            Oe = !0;
          function Ae(e) {
            Oe = e;
          }
          var ke = function (e) {
            (this.value = e),
              (this.dep = new _e()),
              (this.vmCount = 0),
              H(e, "__ob__", this),
              Array.isArray(e)
                ? (G
                    ? e.push !== e.__proto__.push
                      ? $e(e, we, Se)
                      : (function (e, t) {
                          e.__proto__ = t;
                        })(e, we)
                    : $e(e, we, Se),
                  this.observeArray(e))
                : this.walk(e);
          };
          function $e(e, t, n) {
            for (var r = 0, a = n.length; r < a; r++) {
              var i = n[r];
              H(e, i, t[i]);
            }
          }
          function Ee(e, t) {
            var n;
            if (c(e) && !(e instanceof ve))
              return (
                x(e, "__ob__") && e.__ob__ instanceof ke
                  ? (n = e.__ob__)
                  : !Oe ||
                    ne() ||
                    (!Array.isArray(e) && !p(e)) ||
                    !Object.isExtensible(e) ||
                    e._isVue ||
                    e.__v_isMPComponent ||
                    (n = new ke(e)),
                t && n && n.vmCount++,
                n
              );
          }
          function je(e, t, n, r, a) {
            var i = new _e(),
              o = Object.getOwnPropertyDescriptor(e, t);
            if (!o || !1 !== o.configurable) {
              var s = o && o.get,
                c = o && o.set;
              (s && !c) || 2 !== arguments.length || (n = e[t]);
              var u = !a && Ee(n);
              Object.defineProperty(e, t, {
                enumerable: !0,
                configurable: !0,
                get: function () {
                  var t = s ? s.call(e) : n;
                  return (
                    _e.SharedObject.target &&
                      (i.depend(),
                      u && (u.dep.depend(), Array.isArray(t) && Ie(t))),
                    t
                  );
                },
                set: function (t) {
                  var o = s ? s.call(e) : n;
                  t === o ||
                    (t != t && o != o) ||
                    (r && r(),
                    (s && !c) ||
                      (c ? c.call(e, t) : (n = t),
                      (u = !a && Ee(t)),
                      i.notify()));
                },
              });
            }
          }
          function Pe(e, t, n) {
            if (
              ((a(e) || s(e)) &&
                se(
                  "Cannot set reactive property on undefined, null, or primitive value: " +
                    e
                ),
              Array.isArray(e) && d(t))
            )
              return (e.length = Math.max(e.length, t)), e.splice(t, 1, n), n;
            if (t in e && !(t in Object.prototype)) return (e[t] = n), n;
            var r = e.__ob__;
            return e._isVue || (r && r.vmCount)
              ? (se(
                  "Avoid adding reactive properties to a Vue instance or its root $data at runtime - declare it upfront in the data option."
                ),
                n)
              : r
              ? (je(r.value, t, n), r.dep.notify(), n)
              : ((e[t] = n), n);
          }
          function Ce(e, t) {
            if (
              ((a(e) || s(e)) &&
                se(
                  "Cannot delete reactive property on undefined, null, or primitive value: " +
                    e
                ),
              Array.isArray(e) && d(t))
            )
              e.splice(t, 1);
            else {
              var n = e.__ob__;
              e._isVue || (n && n.vmCount)
                ? se(
                    "Avoid deleting properties on a Vue instance or its root $data - just set it to null."
                  )
                : x(e, t) && (delete e[t], n && n.dep.notify());
            }
          }
          function Ie(e) {
            for (var t = void 0, n = 0, r = e.length; n < r; n++)
              (t = e[n]) && t.__ob__ && t.__ob__.dep.depend(),
                Array.isArray(t) && Ie(t);
          }
          (ke.prototype.walk = function (e) {
            for (var t = Object.keys(e), n = 0; n < t.length; n++) je(e, t[n]);
          }),
            (ke.prototype.observeArray = function (e) {
              for (var t = 0, n = e.length; t < n; t++) Ee(e[t]);
            });
          var Te = V.optionMergeStrategies;
          function Le(e, t) {
            if (!t) return e;
            for (
              var n, r, a, i = oe ? Reflect.ownKeys(t) : Object.keys(t), o = 0;
              o < i.length;
              o++
            )
              "__ob__" !== (n = i[o]) &&
                ((r = e[n]),
                (a = t[n]),
                x(e, n) ? r !== a && p(r) && p(a) && Le(r, a) : Pe(e, n, a));
            return e;
          }
          function De(e, t, n) {
            return n
              ? function () {
                  var r = "function" == typeof t ? t.call(n, n) : t,
                    a = "function" == typeof e ? e.call(n, n) : e;
                  return r ? Le(r, a) : a;
                }
              : t
              ? e
                ? function () {
                    return Le(
                      "function" == typeof t ? t.call(this, this) : t,
                      "function" == typeof e ? e.call(this, this) : e
                    );
                  }
                : t
              : e;
          }
          function Me(e, t) {
            var n = t ? (e ? e.concat(t) : Array.isArray(t) ? t : [t]) : e;
            return n
              ? (function (e) {
                  for (var t = [], n = 0; n < e.length; n++)
                    -1 === t.indexOf(e[n]) && t.push(e[n]);
                  return t;
                })(n)
              : n;
          }
          function Be(e, t, n, r) {
            var a = Object.create(e || null);
            return t ? (Ve(r, t, n), P(a, t)) : a;
          }
          (Te.el = Te.propsData =
            function (e, t, n, r) {
              return (
                n ||
                  se(
                    'option "' +
                      r +
                      '" can only be used during instance creation with the `new` keyword.'
                  ),
                Fe(e, t)
              );
            }),
            (Te.data = function (e, t, n) {
              return n
                ? De(e, t, n)
                : t && "function" != typeof t
                ? (se(
                    'The "data" option should be a function that returns a per-instance value in component definitions.',
                    n
                  ),
                  e)
                : De(e, t);
            }),
            N.forEach(function (e) {
              Te[e] = Me;
            }),
            F.forEach(function (e) {
              Te[e + "s"] = Be;
            }),
            (Te.watch = function (e, t, n, r) {
              if ((e === ee && (e = void 0), t === ee && (t = void 0), !t))
                return Object.create(e || null);
              if ((Ve(r, t, n), !e)) return t;
              var a = {};
              for (var i in (P(a, e), t)) {
                var o = a[i],
                  s = t[i];
                o && !Array.isArray(o) && (o = [o]),
                  (a[i] = o ? o.concat(s) : Array.isArray(s) ? s : [s]);
              }
              return a;
            }),
            (Te.props =
              Te.methods =
              Te.inject =
              Te.computed =
                function (e, t, n, r) {
                  if ((t && Ve(r, t, n), !e)) return t;
                  var a = Object.create(null);
                  return P(a, e), t && P(a, t), a;
                }),
            (Te.provide = De);
          var Fe = function (e, t) {
            return void 0 === t ? e : t;
          };
          function Ne(e) {
            new RegExp("^[a-zA-Z][\\-\\.0-9_" + R.source + "]*$").test(e) ||
              se(
                'Invalid component name: "' +
                  e +
                  '". Component names should conform to valid custom element name in html5 specification.'
              ),
              (v(e) || V.isReservedTag(e)) &&
                se(
                  "Do not use built-in or reserved HTML elements as component id: " +
                    e
                );
          }
          function Ve(e, t, n) {
            p(t) ||
              se(
                'Invalid value for option "' +
                  e +
                  '": expected an Object, but got ' +
                  l(t) +
                  ".",
                n
              );
          }
          function Re(e, t, n) {
            if (
              ((function (e) {
                for (var t in e.components) Ne(t);
              })(t),
              "function" == typeof t && (t = t.options),
              (function (e, t) {
                var n = e.props;
                if (n) {
                  var r,
                    a,
                    i = {};
                  if (Array.isArray(n))
                    for (r = n.length; r--; )
                      "string" == typeof (a = n[r])
                        ? (i[O(a)] = { type: null })
                        : se("props must be strings when using array syntax.");
                  else if (p(n))
                    for (var o in n)
                      (a = n[o]), (i[O(o)] = p(a) ? a : { type: a });
                  else
                    se(
                      'Invalid value for option "props": expected an Array or an Object, but got ' +
                        l(n) +
                        ".",
                      t
                    );
                  e.props = i;
                }
              })(t, n),
              (function (e, t) {
                var n = e.inject;
                if (n) {
                  var r = (e.inject = {});
                  if (Array.isArray(n))
                    for (var a = 0; a < n.length; a++) r[n[a]] = { from: n[a] };
                  else if (p(n))
                    for (var i in n) {
                      var o = n[i];
                      r[i] = p(o) ? P({ from: i }, o) : { from: o };
                    }
                  else
                    se(
                      'Invalid value for option "inject": expected an Array or an Object, but got ' +
                        l(n) +
                        ".",
                      t
                    );
                }
              })(t, n),
              (function (e) {
                var t = e.directives;
                if (t)
                  for (var n in t) {
                    var r = t[n];
                    "function" == typeof r && (t[n] = { bind: r, update: r });
                  }
              })(t),
              !t._base && (t.extends && (e = Re(e, t.extends, n)), t.mixins))
            )
              for (var r = 0, a = t.mixins.length; r < a; r++)
                e = Re(e, t.mixins[r], n);
            var i,
              o = {};
            for (i in e) s(i);
            for (i in t) x(e, i) || s(i);
            function s(r) {
              var a = Te[r] || Fe;
              o[r] = a(e[r], t[r], n, r);
            }
            return o;
          }
          function Ue(e, t, n, r) {
            if ("string" == typeof n) {
              var a = e[t];
              if (x(a, n)) return a[n];
              var i = O(n);
              if (x(a, i)) return a[i];
              var o = A(i);
              if (x(a, o)) return a[o];
              var s = a[n] || a[i] || a[o];
              return (
                r &&
                  !s &&
                  se("Failed to resolve " + t.slice(0, -1) + ": " + n, e),
                s
              );
            }
          }
          function He(e, t, n, r) {
            var a = t[e],
              i = !x(n, e),
              o = n[e],
              s = Je(Boolean, a.type);
            if (s > -1)
              if (i && !x(a, "default")) o = !1;
              else if ("" === o || o === $(e)) {
                var u = Je(String, a.type);
                (u < 0 || s < u) && (o = !0);
              }
            if (void 0 === o) {
              o = (function (e, t, n) {
                if (!x(t, "default")) return;
                var r = t.default;
                c(r) &&
                  se(
                    'Invalid default value for prop "' +
                      n +
                      '": Props with type Object/Array must use a factory function to return the default value.',
                    e
                  );
                if (
                  e &&
                  e.$options.propsData &&
                  void 0 === e.$options.propsData[n] &&
                  void 0 !== e._props[n]
                )
                  return e._props[n];
                return "function" == typeof r && "Function" !== Ge(t.type)
                  ? r.call(e)
                  : r;
              })(r, a, e);
              var p = Oe;
              Ae(!0), Ee(o), Ae(p);
            }
            return (
              (function (e, t, n, r, a) {
                if (e.required && a)
                  return void se('Missing required prop: "' + t + '"', r);
                if (null == n && !e.required) return;
                var i = e.type,
                  o = !i || !0 === i,
                  s = [];
                if (i) {
                  Array.isArray(i) || (i = [i]);
                  for (var c = 0; c < i.length && !o; c++) {
                    var u = qe(n, i[c]);
                    s.push(u.expectedType || ""), (o = u.valid);
                  }
                }
                if (!o)
                  return void se(
                    (function (e, t, n) {
                      var r =
                          'Invalid prop: type check failed for prop "' +
                          e +
                          '". Expected ' +
                          n.map(A).join(", "),
                        a = n[0],
                        i = l(t),
                        o = We(t, a),
                        s = We(t, i);
                      1 === n.length &&
                        Xe(a) &&
                        !(function () {
                          var e = [],
                            t = arguments.length;
                          for (; t--; ) e[t] = arguments[t];
                          return e.some(function (e) {
                            return "boolean" === e.toLowerCase();
                          });
                        })(a, i) &&
                        (r += " with value " + o);
                      (r += ", got " + i + " "),
                        Xe(i) && (r += "with value " + s + ".");
                      return r;
                    })(t, n, s),
                    r
                  );
                var p = e.validator;
                p &&
                  (p(n) ||
                    se(
                      'Invalid prop: custom validator check failed for prop "' +
                        t +
                        '".',
                      r
                    ));
              })(a, e, o, r, i),
              o
            );
          }
          var Ke = /^(String|Number|Boolean|Function|Symbol)$/;
          function qe(t, n) {
            var r,
              a = Ge(n);
            if (Ke.test(a)) {
              var i = e(t);
              (r = i === a.toLowerCase()) ||
                "object" !== i ||
                (r = t instanceof n);
            } else
              r =
                "Object" === a
                  ? p(t)
                  : "Array" === a
                  ? Array.isArray(t)
                  : t instanceof n;
            return { valid: r, expectedType: a };
          }
          function Ge(e) {
            var t = e && e.toString().match(/^\s*function (\w+)/);
            return t ? t[1] : "";
          }
          function ze(e, t) {
            return Ge(e) === Ge(t);
          }
          function Je(e, t) {
            if (!Array.isArray(t)) return ze(t, e) ? 0 : -1;
            for (var n = 0, r = t.length; n < r; n++) if (ze(t[n], e)) return n;
            return -1;
          }
          function We(e, t) {
            return "String" === t
              ? '"' + e + '"'
              : "Number" === t
              ? "" + Number(e)
              : "" + e;
          }
          function Xe(e) {
            return ["string", "number", "boolean"].some(function (t) {
              return e.toLowerCase() === t;
            });
          }
          function Ze(e, t, n) {
            he();
            try {
              if (t)
                for (var r = t; (r = r.$parent); ) {
                  var a = r.$options.errorCaptured;
                  if (a)
                    for (var i = 0; i < a.length; i++)
                      try {
                        if (!1 === a[i].call(r, e, t, n)) return;
                      } catch (e) {
                        Qe(e, r, "errorCaptured hook");
                      }
                }
              Qe(e, t, n);
            } finally {
              me();
            }
          }
          function Ye(e, t, n, r, a) {
            var i;
            try {
              (i = n ? e.apply(t, n) : e.call(t)) &&
                !i._isVue &&
                f(i) &&
                !i._handled &&
                (i.catch(function (e) {
                  return Ze(e, r, a + " (Promise/async)");
                }),
                (i._handled = !0));
            } catch (e) {
              Ze(e, r, a);
            }
            return i;
          }
          function Qe(e, t, n) {
            if (V.errorHandler)
              try {
                return V.errorHandler.call(null, e, t, n);
              } catch (t) {
                t !== e && et(t, null, "config.errorHandler");
              }
            et(e, t, n);
          }
          function et(e, t, n) {
            if (
              (se("Error in " + n + ': "' + e.toString() + '"', t),
              (!z && !J) || "undefined" == typeof console)
            )
              throw e;
            console.error(e);
          }
          var tt,
            nt,
            rt = [],
            at = !1;
          function it() {
            at = !1;
            var e = rt.slice(0);
            rt.length = 0;
            for (var t = 0; t < e.length; t++) e[t]();
          }
          if ("undefined" != typeof Promise && ae(Promise)) {
            var ot = Promise.resolve();
            tt = function () {
              ot.then(it), Q && setTimeout(I);
            };
          } else if (
            Z ||
            "undefined" == typeof MutationObserver ||
            (!ae(MutationObserver) &&
              "[object MutationObserverConstructor]" !==
                MutationObserver.toString())
          )
            tt =
              "undefined" != typeof setImmediate && ae(setImmediate)
                ? function () {
                    setImmediate(it);
                  }
                : function () {
                    setTimeout(it, 0);
                  };
          else {
            var st = 1,
              ct = new MutationObserver(it),
              ut = document.createTextNode(String(st));
            ct.observe(ut, { characterData: !0 }),
              (tt = function () {
                (st = (st + 1) % 2), (ut.data = String(st));
              });
          }
          function lt(e, t) {
            var n;
            if (
              (rt.push(function () {
                if (e)
                  try {
                    e.call(t);
                  } catch (e) {
                    Ze(e, t, "nextTick");
                  }
                else n && n(t);
              }),
              at || ((at = !0), tt()),
              !e && "undefined" != typeof Promise)
            )
              return new Promise(function (e) {
                n = e;
              });
          }
          var pt = m(
              "Infinity,undefined,NaN,isFinite,isNaN,parseFloat,parseInt,decodeURI,decodeURIComponent,encodeURI,encodeURIComponent,Math,Number,Date,Array,Object,Boolean,String,RegExp,Map,Set,JSON,Intl,require"
            ),
            dt = function (e, t) {
              se(
                'Property or method "' +
                  t +
                  '" is not defined on the instance but referenced during render. Make sure that this property is reactive, either in the data option, or for class-based components, by initializing the property. See: https://vuejs.org/v2/guide/reactivity.html#Declaring-Reactive-Properties.',
                e
              );
            },
            ft = function (e, t) {
              se(
                'Property "' +
                  t +
                  '" must be accessed with "$data.' +
                  t +
                  '" because properties starting with "$" or "_" are not proxied in the Vue instance to prevent conflicts with Vue internals. See: https://vuejs.org/v2/api/#data',
                e
              );
            },
            _t = "undefined" != typeof Proxy && ae(Proxy);
          if (_t) {
            var ht = m("stop,prevent,self,ctrl,shift,alt,meta,exact");
            V.keyCodes = new Proxy(V.keyCodes, {
              set: function (e, t, n) {
                return ht(t)
                  ? (se(
                      "Avoid overwriting built-in modifier in config.keyCodes: ." +
                        t
                    ),
                    !1)
                  : ((e[t] = n), !0);
              },
            });
          }
          var mt = {
              has: function (e, t) {
                var n = t in e,
                  r =
                    pt(t) ||
                    ("string" == typeof t &&
                      "_" === t.charAt(0) &&
                      !(t in e.$data));
                return n || r || (t in e.$data ? ft(e, t) : dt(e, t)), n || !r;
              },
            },
            vt = {
              get: function (e, t) {
                return (
                  "string" != typeof t ||
                    t in e ||
                    (t in e.$data ? ft(e, t) : dt(e, t)),
                  e[t]
                );
              },
            };
          nt = function (e) {
            if (_t) {
              var t = e.$options,
                n = t.render && t.render._withStripped ? vt : mt;
              e._renderProxy = new Proxy(e, n);
            } else e._renderProxy = e;
          };
          var gt,
            bt,
            yt = new ie();
          function xt(e) {
            !(function e(t, n) {
              var r,
                a,
                i = Array.isArray(t);
              if ((!i && !c(t)) || Object.isFrozen(t) || t instanceof ve)
                return;
              if (t.__ob__) {
                var o = t.__ob__.dep.id;
                if (n.has(o)) return;
                n.add(o);
              }
              if (i) for (r = t.length; r--; ) e(t[r], n);
              else for (a = Object.keys(t), r = a.length; r--; ) e(t[a[r]], n);
            })(e, yt),
              yt.clear();
          }
          var wt = z && window.performance;
          wt &&
            wt.mark &&
            wt.measure &&
            wt.clearMarks &&
            wt.clearMeasures &&
            ((gt = function (e) {
              return wt.mark(e);
            }),
            (bt = function (e, t, n) {
              wt.measure(e, t, n), wt.clearMarks(t), wt.clearMarks(n);
            }));
          var St = w(function (e) {
            var t = "&" === e.charAt(0),
              n = "~" === (e = t ? e.slice(1) : e).charAt(0),
              r = "!" === (e = n ? e.slice(1) : e).charAt(0);
            return {
              name: (e = r ? e.slice(1) : e),
              once: n,
              capture: r,
              passive: t,
            };
          });
          function Ot(e, t) {
            function n() {
              var e = arguments,
                r = n.fns;
              if (!Array.isArray(r))
                return Ye(r, null, arguments, t, "v-on handler");
              for (var a = r.slice(), i = 0; i < a.length; i++)
                Ye(a[i], null, e, t, "v-on handler");
            }
            return (n.fns = e), n;
          }
          function At(e, t, n, r) {
            var o = t.options.mpOptions && t.options.mpOptions.properties;
            if (a(o)) return n;
            var s = t.options.mpOptions.externalClasses || [],
              c = e.attrs,
              u = e.props;
            if (i(c) || i(u))
              for (var l in o) {
                var p = $(l);
                (kt(n, u, l, p, !0) || kt(n, c, l, p, !1)) &&
                  n[l] &&
                  -1 !== s.indexOf(p) &&
                  r[O(n[l])] &&
                  (n[l] = r[O(n[l])]);
              }
            return n;
          }
          function kt(e, t, n, r, a) {
            if (i(t)) {
              if (x(t, n)) return (e[n] = t[n]), a || delete t[n], !0;
              if (x(t, r)) return (e[n] = t[r]), a || delete t[r], !0;
            }
            return !1;
          }
          function $t(e) {
            return s(e)
              ? [ye(e)]
              : Array.isArray(e)
              ? (function e(t, n) {
                  var r,
                    c,
                    u,
                    l,
                    p = [];
                  for (r = 0; r < t.length; r++)
                    a((c = t[r])) ||
                      "boolean" == typeof c ||
                      ((u = p.length - 1),
                      (l = p[u]),
                      Array.isArray(c)
                        ? c.length > 0 &&
                          (Et((c = e(c, (n || "") + "_" + r))[0]) &&
                            Et(l) &&
                            ((p[u] = ye(l.text + c[0].text)), c.shift()),
                          p.push.apply(p, c))
                        : s(c)
                        ? Et(l)
                          ? (p[u] = ye(l.text + c))
                          : "" !== c && p.push(ye(c))
                        : Et(c) && Et(l)
                        ? (p[u] = ye(l.text + c.text))
                        : (o(t._isVList) &&
                            i(c.tag) &&
                            a(c.key) &&
                            i(n) &&
                            (c.key = "__vlist" + n + "_" + r + "__"),
                          p.push(c)));
                  return p;
                })(e)
              : void 0;
          }
          function Et(e) {
            return i(e) && i(e.text) && !1 === e.isComment;
          }
          function jt(e) {
            var t = e.$options.provide;
            t && (e._provided = "function" == typeof t ? t.call(e) : t);
          }
          function Pt(e) {
            var t = Ct(e.$options.inject, e);
            t &&
              (Ae(!1),
              Object.keys(t).forEach(function (n) {
                je(e, n, t[n], function () {
                  se(
                    'Avoid mutating an injected value directly since the changes will be overwritten whenever the provided component re-renders. injection being mutated: "' +
                      n +
                      '"',
                    e
                  );
                });
              }),
              Ae(!0));
          }
          function Ct(e, t) {
            if (e) {
              for (
                var n = Object.create(null),
                  r = oe ? Reflect.ownKeys(e) : Object.keys(e),
                  a = 0;
                a < r.length;
                a++
              ) {
                var i = r[a];
                if ("__ob__" !== i) {
                  for (var o = e[i].from, s = t; s; ) {
                    if (s._provided && x(s._provided, o)) {
                      n[i] = s._provided[o];
                      break;
                    }
                    s = s.$parent;
                  }
                  if (!s)
                    if ("default" in e[i]) {
                      var c = e[i].default;
                      n[i] = "function" == typeof c ? c.call(t) : c;
                    } else se('Injection "' + i + '" not found', t);
                }
              }
              return n;
            }
          }
          function It(e, t) {
            if (!e || !e.length) return {};
            for (var n = {}, r = 0, a = e.length; r < a; r++) {
              var i = e[r],
                o = i.data;
              if (
                (o && o.attrs && o.attrs.slot && delete o.attrs.slot,
                (i.context !== t && i.fnContext !== t) || !o || null == o.slot)
              )
                i.asyncMeta &&
                i.asyncMeta.data &&
                "page" === i.asyncMeta.data.slot
                  ? (n.page || (n.page = [])).push(i)
                  : (n.default || (n.default = [])).push(i);
              else {
                var s = o.slot,
                  c = n[s] || (n[s] = []);
                "template" === i.tag
                  ? c.push.apply(c, i.children || [])
                  : c.push(i);
              }
            }
            for (var u in n) n[u].every(Tt) && delete n[u];
            return n;
          }
          function Tt(e) {
            return (e.isComment && !e.asyncFactory) || " " === e.text;
          }
          function Lt(e, t, n) {
            var a,
              i = Object.keys(t).length > 0,
              o = e ? !!e.$stable : !i,
              s = e && e.$key;
            if (e) {
              if (e._normalized) return e._normalized;
              if (o && n && n !== r && s === n.$key && !i && !n.$hasNormal)
                return n;
              for (var c in ((a = {}), e))
                e[c] && "$" !== c[0] && (a[c] = Dt(t, c, e[c]));
            } else a = {};
            for (var u in t) u in a || (a[u] = Mt(t, u));
            return (
              e && Object.isExtensible(e) && (e._normalized = a),
              H(a, "$stable", o),
              H(a, "$key", s),
              H(a, "$hasNormal", i),
              a
            );
          }
          function Dt(t, n, r) {
            var a = function () {
              var t = arguments.length ? r.apply(null, arguments) : r({});
              return (t =
                t && "object" === e(t) && !Array.isArray(t) ? [t] : $t(t)) &&
                (0 === t.length || (1 === t.length && t[0].isComment))
                ? void 0
                : t;
            };
            return (
              r.proxy &&
                Object.defineProperty(t, n, {
                  get: a,
                  enumerable: !0,
                  configurable: !0,
                }),
              a
            );
          }
          function Mt(e, t) {
            return function () {
              return e[t];
            };
          }
          function Bt(e, t) {
            var n, r, a, o, s;
            if (Array.isArray(e) || "string" == typeof e)
              for (n = new Array(e.length), r = 0, a = e.length; r < a; r++)
                n[r] = t(e[r], r, r, r);
            else if ("number" == typeof e)
              for (n = new Array(e), r = 0; r < e; r++)
                n[r] = t(r + 1, r, r, r);
            else if (c(e))
              if (oe && e[Symbol.iterator]) {
                n = [];
                for (var u = e[Symbol.iterator](), l = u.next(); !l.done; )
                  n.push(t(l.value, n.length, r, r++)), (l = u.next());
              } else
                for (
                  o = Object.keys(e),
                    n = new Array(o.length),
                    r = 0,
                    a = o.length;
                  r < a;
                  r++
                )
                  (s = o[r]), (n[r] = t(e[s], s, r, r));
            return i(n) || (n = []), (n._isVList = !0), n;
          }
          function Ft(e, t, n, r) {
            var a,
              i = this.$scopedSlots[e];
            i
              ? ((n = n || {}),
                r &&
                  (c(r) ||
                    se("slot v-bind without argument expects an Object", this),
                  (n = P(P({}, r), n))),
                (a = i(n, this, n._i) || t))
              : (a = this.$slots[e] || t);
            var o = n && n.slot;
            return o ? this.$createElement("template", { slot: o }, a) : a;
          }
          function Nt(e) {
            return Ue(this.$options, "filters", e, !0) || L;
          }
          function Vt(e, t) {
            return Array.isArray(e) ? -1 === e.indexOf(t) : e !== t;
          }
          function Rt(e, t, n, r, a) {
            var i = V.keyCodes[t] || n;
            return a && r && !V.keyCodes[t]
              ? Vt(a, r)
              : i
              ? Vt(i, e)
              : r
              ? $(r) !== t
              : void 0;
          }
          function Ut(e, t, n, r, a) {
            if (n)
              if (c(n)) {
                var i;
                Array.isArray(n) && (n = C(n));
                var o = function (o) {
                  if ("class" === o || "style" === o || g(o)) i = e;
                  else {
                    var s = e.attrs && e.attrs.type;
                    i =
                      r || V.mustUseProp(t, s, o)
                        ? e.domProps || (e.domProps = {})
                        : e.attrs || (e.attrs = {});
                  }
                  var c = O(o),
                    u = $(o);
                  c in i ||
                    u in i ||
                    ((i[o] = n[o]),
                    a &&
                      ((e.on || (e.on = {}))["update:" + o] = function (e) {
                        n[o] = e;
                      }));
                };
                for (var s in n) o(s);
              } else
                se(
                  "v-bind without argument expects an Object or Array value",
                  this
                );
            return e;
          }
          function Ht(e, t) {
            var n = this._staticTrees || (this._staticTrees = []),
              r = n[e];
            return (
              (r && !t) ||
                qt(
                  (r = n[e] =
                    this.$options.staticRenderFns[e].call(
                      this._renderProxy,
                      null,
                      this
                    )),
                  "__static__" + e,
                  !1
                ),
              r
            );
          }
          function Kt(e, t, n) {
            return qt(e, "__once__" + t + (n ? "_" + n : ""), !0), e;
          }
          function qt(e, t, n) {
            if (Array.isArray(e))
              for (var r = 0; r < e.length; r++)
                e[r] && "string" != typeof e[r] && Gt(e[r], t + "_" + r, n);
            else Gt(e, t, n);
          }
          function Gt(e, t, n) {
            (e.isStatic = !0), (e.key = t), (e.isOnce = n);
          }
          function zt(e, t) {
            if (t)
              if (p(t)) {
                var n = (e.on = e.on ? P({}, e.on) : {});
                for (var r in t) {
                  var a = n[r],
                    i = t[r];
                  n[r] = a ? [].concat(a, i) : i;
                }
              } else se("v-on without argument expects an Object value", this);
            return e;
          }
          function Jt(e, t, n, r) {
            t = t || { $stable: !n };
            for (var a = 0; a < e.length; a++) {
              var i = e[a];
              Array.isArray(i)
                ? Jt(i, t, n)
                : i && (i.proxy && (i.fn.proxy = !0), (t[i.key] = i.fn));
            }
            return r && (t.$key = r), t;
          }
          function Wt(e, t) {
            for (var n = 0; n < t.length; n += 2) {
              var r = t[n];
              "string" == typeof r && r
                ? (e[t[n]] = t[n + 1])
                : "" !== r &&
                  null !== r &&
                  se(
                    "Invalid value for dynamic directive argument (expected string or null): " +
                      r,
                    this
                  );
            }
            return e;
          }
          function Xt(e, t) {
            return "string" == typeof e ? t + e : e;
          }
          function Zt(e) {
            (e._o = Kt),
              (e._n = h),
              (e._s = _),
              (e._l = Bt),
              (e._t = Ft),
              (e._q = D),
              (e._i = M),
              (e._m = Ht),
              (e._f = Nt),
              (e._k = Rt),
              (e._b = Ut),
              (e._v = ye),
              (e._e = be),
              (e._u = Jt),
              (e._g = zt),
              (e._d = Wt),
              (e._p = Xt);
          }
          function Yt(e, t, n, a, i) {
            var s,
              c = this,
              u = i.options;
            x(a, "_uid")
              ? ((s = Object.create(a))._original = a)
              : ((s = a), (a = a._original));
            var l = o(u._compiled),
              p = !l;
            (this.data = e),
              (this.props = t),
              (this.children = n),
              (this.parent = a),
              (this.listeners = e.on || r),
              (this.injections = Ct(u.inject, a)),
              (this.slots = function () {
                return (
                  c.$slots || Lt(e.scopedSlots, (c.$slots = It(n, a))), c.$slots
                );
              }),
              Object.defineProperty(this, "scopedSlots", {
                enumerable: !0,
                get: function () {
                  return Lt(e.scopedSlots, this.slots());
                },
              }),
              l &&
                ((this.$options = u),
                (this.$slots = this.slots()),
                (this.$scopedSlots = Lt(e.scopedSlots, this.$slots))),
              u._scopeId
                ? (this._c = function (e, t, n, r) {
                    var i = on(s, e, t, n, r, p);
                    return (
                      i &&
                        !Array.isArray(i) &&
                        ((i.fnScopeId = u._scopeId), (i.fnContext = a)),
                      i
                    );
                  })
                : (this._c = function (e, t, n, r) {
                    return on(s, e, t, n, r, p);
                  });
          }
          function Qt(e, t, n, r, a) {
            var i = (function (e) {
              var t = new ve(
                e.tag,
                e.data,
                e.children && e.children.slice(),
                e.text,
                e.elm,
                e.context,
                e.componentOptions,
                e.asyncFactory
              );
              return (
                (t.ns = e.ns),
                (t.isStatic = e.isStatic),
                (t.key = e.key),
                (t.isComment = e.isComment),
                (t.fnContext = e.fnContext),
                (t.fnOptions = e.fnOptions),
                (t.fnScopeId = e.fnScopeId),
                (t.asyncMeta = e.asyncMeta),
                (t.isCloned = !0),
                t
              );
            })(e);
            return (
              (i.fnContext = n),
              (i.fnOptions = r),
              ((i.devtoolsMeta = i.devtoolsMeta || {}).renderContext = a),
              t.slot && ((i.data || (i.data = {})).slot = t.slot),
              i
            );
          }
          function en(e, t) {
            for (var n in t) e[O(n)] = t[n];
          }
          Zt(Yt.prototype);
          var tn = {
              init: function (e, t) {
                if (
                  e.componentInstance &&
                  !e.componentInstance._isDestroyed &&
                  e.data.keepAlive
                ) {
                  var n = e;
                  tn.prepatch(n, n);
                } else {
                  (e.componentInstance = (function (e, t) {
                    var n = { _isComponent: !0, _parentVnode: e, parent: t },
                      r = e.data.inlineTemplate;
                    i(r) &&
                      ((n.render = r.render),
                      (n.staticRenderFns = r.staticRenderFns));
                    return new e.componentOptions.Ctor(n);
                  })(e, _n)).$mount(t ? e.elm : void 0, t);
                }
              },
              prepatch: function (e, t) {
                var n = t.componentOptions;
                !(function (e, t, n, a, i) {
                  hn = !0;
                  var o = a.data.scopedSlots,
                    s = e.$scopedSlots,
                    c = !!(
                      (o && !o.$stable) ||
                      (s !== r && !s.$stable) ||
                      (o && e.$scopedSlots.$key !== o.$key)
                    ),
                    u = !!(i || e.$options._renderChildren || c);
                  (e.$options._parentVnode = a),
                    (e.$vnode = a),
                    e._vnode && (e._vnode.parent = a);
                  if (
                    ((e.$options._renderChildren = i),
                    (e.$attrs = a.data.attrs || r),
                    (e.$listeners = n || r),
                    t && e.$options.props)
                  ) {
                    Ae(!1);
                    for (
                      var l = e._props, p = e.$options._propKeys || [], d = 0;
                      d < p.length;
                      d++
                    ) {
                      var f = p[d],
                        _ = e.$options.props;
                      l[f] = He(f, _, t, e);
                    }
                    Ae(!0), (e.$options.propsData = t);
                  }
                  e._$updateProperties && e._$updateProperties(e), (n = n || r);
                  var h = e.$options._parentListeners;
                  (e.$options._parentListeners = n),
                    fn(e, n, h),
                    u && ((e.$slots = It(i, a.context)), e.$forceUpdate());
                  hn = !1;
                })(
                  (t.componentInstance = e.componentInstance),
                  n.propsData,
                  n.listeners,
                  t,
                  n.children
                );
              },
              insert: function (e) {
                var t,
                  n = e.context,
                  r = e.componentInstance;
                r._isMounted ||
                  (gn(r, "onServiceCreated"),
                  gn(r, "onServiceAttached"),
                  (r._isMounted = !0),
                  gn(r, "mounted")),
                  e.data.keepAlive &&
                    (n._isMounted
                      ? (((t = r)._inactive = !1), yn.push(t))
                      : vn(r, !0));
              },
              destroy: function (e) {
                var t = e.componentInstance;
                t._isDestroyed ||
                  (e.data.keepAlive
                    ? (function e(t, n) {
                        if (n && ((t._directInactive = !0), mn(t))) return;
                        if (!t._inactive) {
                          t._inactive = !0;
                          for (var r = 0; r < t.$children.length; r++)
                            e(t.$children[r]);
                          gn(t, "deactivated");
                        }
                      })(t, !0)
                    : t.$destroy());
              },
            },
            nn = Object.keys(tn);
          function rn(e, t, n, s, u) {
            if (!a(e)) {
              var l = n.$options._base;
              if ((c(e) && (e = l.extend(e)), "function" == typeof e)) {
                var p;
                if (
                  a(e.cid) &&
                  void 0 ===
                    (e = (function (e, t) {
                      if (o(e.error) && i(e.errorComp)) return e.errorComp;
                      if (i(e.resolved)) return e.resolved;
                      var n = cn;
                      n &&
                        i(e.owners) &&
                        -1 === e.owners.indexOf(n) &&
                        e.owners.push(n);
                      if (o(e.loading) && i(e.loadingComp))
                        return e.loadingComp;
                      if (n && !i(e.owners)) {
                        var r = (e.owners = [n]),
                          s = !0,
                          u = null,
                          l = null;
                        n.$on("hook:destroyed", function () {
                          return b(r, n);
                        });
                        var p = function (e) {
                            for (var t = 0, n = r.length; t < n; t++)
                              r[t].$forceUpdate();
                            e &&
                              ((r.length = 0),
                              null !== u && (clearTimeout(u), (u = null)),
                              null !== l && (clearTimeout(l), (l = null)));
                          },
                          d = B(function (n) {
                            (e.resolved = un(n, t)), s ? (r.length = 0) : p(!0);
                          }),
                          _ = B(function (t) {
                            se(
                              "Failed to resolve async component: " +
                                String(e) +
                                (t ? "\nReason: " + t : "")
                            ),
                              i(e.errorComp) && ((e.error = !0), p(!0));
                          }),
                          h = e(d, _);
                        return (
                          c(h) &&
                            (f(h)
                              ? a(e.resolved) && h.then(d, _)
                              : f(h.component) &&
                                (h.component.then(d, _),
                                i(h.error) && (e.errorComp = un(h.error, t)),
                                i(h.loading) &&
                                  ((e.loadingComp = un(h.loading, t)),
                                  0 === h.delay
                                    ? (e.loading = !0)
                                    : (u = setTimeout(function () {
                                        (u = null),
                                          a(e.resolved) &&
                                            a(e.error) &&
                                            ((e.loading = !0), p(!1));
                                      }, h.delay || 200))),
                                i(h.timeout) &&
                                  (l = setTimeout(function () {
                                    (l = null),
                                      a(e.resolved) &&
                                        _("timeout (" + h.timeout + "ms)");
                                  }, h.timeout)))),
                          (s = !1),
                          e.loading ? e.loadingComp : e.resolved
                        );
                      }
                    })((p = e), l))
                )
                  return (function (e, t, n, r, a) {
                    var i = be();
                    return (
                      (i.asyncFactory = e),
                      (i.asyncMeta = {
                        data: t,
                        context: n,
                        children: r,
                        tag: a,
                      }),
                      i
                    );
                  })(p, t, n, s, u);
                (t = t || {}),
                  Vn(e),
                  i(t.model) &&
                    (function (e, t) {
                      var n = (e.model && e.model.prop) || "value",
                        r = (e.model && e.model.event) || "input";
                      (t.attrs || (t.attrs = {}))[n] = t.model.value;
                      var a = t.on || (t.on = {}),
                        o = a[r],
                        s = t.model.callback;
                      i(o)
                        ? (Array.isArray(o) ? -1 === o.indexOf(s) : o !== s) &&
                          (a[r] = [s].concat(o))
                        : (a[r] = s);
                    })(e.options, t);
                var d = (function (e, t, n, r) {
                  var o = t.options.props;
                  if (a(o)) return At(e, t, {}, r);
                  var s = {},
                    c = e.attrs,
                    u = e.props;
                  if (i(c) || i(u))
                    for (var l in o) {
                      var p = $(l),
                        d = l.toLowerCase();
                      l !== d &&
                        c &&
                        x(c, d) &&
                        ce(
                          'Prop "' +
                            d +
                            '" is passed to component ' +
                            le(n || t) +
                            ', but the declared prop name is "' +
                            l +
                            '". Note that HTML attributes are case-insensitive and camelCased props need to use their kebab-case equivalents when using in-DOM templates. You should probably use "' +
                            p +
                            '" instead of "' +
                            l +
                            '".'
                        ),
                        kt(s, u, l, p, !0) || kt(s, c, l, p, !1);
                    }
                  return At(e, t, s, r);
                })(t, e, u, n);
                if (o(e.options.functional))
                  return (function (e, t, n, a, o) {
                    var s = e.options,
                      c = {},
                      u = s.props;
                    if (i(u)) for (var l in u) c[l] = He(l, u, t || r);
                    else
                      i(n.attrs) && en(c, n.attrs),
                        i(n.props) && en(c, n.props);
                    var p = new Yt(n, c, o, a, e),
                      d = s.render.call(null, p._c, p);
                    if (d instanceof ve) return Qt(d, n, p.parent, s, p);
                    if (Array.isArray(d)) {
                      for (
                        var f = $t(d) || [], _ = new Array(f.length), h = 0;
                        h < f.length;
                        h++
                      )
                        _[h] = Qt(f[h], n, p.parent, s, p);
                      return _;
                    }
                  })(e, d, t, n, s);
                var _ = t.on;
                if (((t.on = t.nativeOn), o(e.options.abstract))) {
                  var h = t.slot;
                  (t = {}), h && (t.slot = h);
                }
                !(function (e) {
                  for (
                    var t = e.hook || (e.hook = {}), n = 0;
                    n < nn.length;
                    n++
                  ) {
                    var r = nn[n],
                      a = t[r],
                      i = tn[r];
                    a === i || (a && a._merged) || (t[r] = a ? an(i, a) : i);
                  }
                })(t);
                var m = e.options.name || u;
                return new ve(
                  "vue-component-" + e.cid + (m ? "-" + m : ""),
                  t,
                  void 0,
                  void 0,
                  void 0,
                  n,
                  { Ctor: e, propsData: d, listeners: _, tag: u, children: s },
                  p
                );
              }
              se("Invalid Component definition: " + String(e), n);
            }
          }
          function an(e, t) {
            var n = function (n, r) {
              e(n, r), t(n, r);
            };
            return (n._merged = !0), n;
          }
          function on(e, t, n, r, u, l) {
            return (
              (Array.isArray(n) || s(n)) && ((u = r), (r = n), (n = void 0)),
              o(l) && (u = 2),
              (function (e, t, n, r, u) {
                if (i(n) && i(n.__ob__))
                  return (
                    se(
                      "Avoid using observed data object as vnode data: " +
                        JSON.stringify(n) +
                        "\nAlways create fresh vnode data objects in each render!",
                      e
                    ),
                    be()
                  );
                i(n) && i(n.is) && (t = n.is);
                if (!t) return be();
                i(n) &&
                  i(n.key) &&
                  !s(n.key) &&
                  se(
                    "Avoid using non-primitive value as key, use string/number value instead.",
                    e
                  );
                Array.isArray(r) &&
                  "function" == typeof r[0] &&
                  (((n = n || {}).scopedSlots = { default: r[0] }),
                  (r.length = 0));
                2 === u
                  ? (r = $t(r))
                  : 1 === u &&
                    (r = (function (e) {
                      for (var t = 0; t < e.length; t++)
                        if (Array.isArray(e[t]))
                          return Array.prototype.concat.apply([], e);
                      return e;
                    })(r));
                var l, p;
                if ("string" == typeof t) {
                  var d;
                  (p = (e.$vnode && e.$vnode.ns) || V.getTagNamespace(t)),
                    V.isReservedTag(t)
                      ? (i(n) &&
                          i(n.nativeOn) &&
                          se(
                            "The .native modifier for v-on is only valid on components but it was used on <" +
                              t +
                              ">.",
                            e
                          ),
                        (l = new ve(
                          V.parsePlatformTagName(t),
                          n,
                          r,
                          void 0,
                          void 0,
                          e
                        )))
                      : (l =
                          (n && n.pre) ||
                          !i((d = Ue(e.$options, "components", t)))
                            ? new ve(t, n, r, void 0, void 0, e)
                            : rn(d, n, e, r, t));
                } else l = rn(t, n, e, r);
                return Array.isArray(l)
                  ? l
                  : i(l)
                  ? (i(p) &&
                      (function e(t, n, r) {
                        (t.ns = n),
                          "foreignObject" === t.tag && ((n = void 0), (r = !0));
                        if (i(t.children))
                          for (var s = 0, c = t.children.length; s < c; s++) {
                            var u = t.children[s];
                            i(u.tag) &&
                              (a(u.ns) || (o(r) && "svg" !== u.tag)) &&
                              e(u, n, r);
                          }
                      })(l, p),
                    i(n) &&
                      (function (e) {
                        c(e.style) && xt(e.style);
                        c(e.class) && xt(e.class);
                      })(n),
                    l)
                  : be();
              })(e, t, n, r, u)
            );
          }
          var sn,
            cn = null;
          function un(e, t) {
            return (
              (e.__esModule || (oe && "Module" === e[Symbol.toStringTag])) &&
                (e = e.default),
              c(e) ? t.extend(e) : e
            );
          }
          function ln(e, t) {
            sn.$on(e, t);
          }
          function pn(e, t) {
            sn.$off(e, t);
          }
          function dn(e, t) {
            var n = sn;
            return function r() {
              var a = t.apply(null, arguments);
              null !== a && n.$off(e, r);
            };
          }
          function fn(e, t, n) {
            (sn = e),
              (function (e, t, n, r, i, s) {
                var c, u, l, p;
                for (c in e)
                  (u = e[c]),
                    (l = t[c]),
                    (p = St(c)),
                    a(u)
                      ? se(
                          'Invalid handler for event "' +
                            p.name +
                            '": got ' +
                            String(u),
                          s
                        )
                      : a(l)
                      ? (a(u.fns) && (u = e[c] = Ot(u, s)),
                        o(p.once) && (u = e[c] = i(p.name, u, p.capture)),
                        n(p.name, u, p.capture, p.passive, p.params))
                      : u !== l && ((l.fns = u), (e[c] = l));
                for (c in t) a(e[c]) && r((p = St(c)).name, t[c], p.capture);
              })(t, n || {}, ln, pn, dn, e),
              (sn = void 0);
          }
          var _n = null,
            hn = !1;
          function mn(e) {
            for (; e && (e = e.$parent); ) if (e._inactive) return !0;
            return !1;
          }
          function vn(e, t) {
            if (t) {
              if (((e._directInactive = !1), mn(e))) return;
            } else if (e._directInactive) return;
            if (e._inactive || null === e._inactive) {
              e._inactive = !1;
              for (var n = 0; n < e.$children.length; n++) vn(e.$children[n]);
              gn(e, "activated");
            }
          }
          function gn(e, t) {
            he();
            var n = e.$options[t],
              r = t + " hook";
            if (n)
              for (var a = 0, i = n.length; a < i; a++) Ye(n[a], e, null, e, r);
            e._hasHookEvent && e.$emit("hook:" + t), me();
          }
          var bn = [],
            yn = [],
            xn = {},
            wn = {},
            Sn = !1,
            On = !1,
            An = 0;
          var kn = Date.now;
          if (z && !Z) {
            var $n = window.performance;
            $n &&
              "function" == typeof $n.now &&
              kn() > document.createEvent("Event").timeStamp &&
              (kn = function () {
                return $n.now();
              });
          }
          function En() {
            var e, t;
            for (
              kn(),
                On = !0,
                bn.sort(function (e, t) {
                  return e.id - t.id;
                }),
                An = 0;
              An < bn.length;
              An++
            )
              if (
                ((e = bn[An]).before && e.before(),
                (t = e.id),
                (xn[t] = null),
                e.run(),
                null != xn[t] && ((wn[t] = (wn[t] || 0) + 1), wn[t] > 100))
              ) {
                se(
                  "You may have an infinite update loop " +
                    (e.user
                      ? 'in watcher with expression "' + e.expression + '"'
                      : "in a component render function."),
                  e.vm
                );
                break;
              }
            var n = yn.slice(),
              r = bn.slice();
            (An = bn.length = yn.length = 0),
              (xn = {}),
              (wn = {}),
              (Sn = On = !1),
              (function (e) {
                for (var t = 0; t < e.length; t++)
                  (e[t]._inactive = !0), vn(e[t], !0);
              })(n),
              (function (e) {
                var t = e.length;
                for (; t--; ) {
                  var n = e[t],
                    r = n.vm;
                  r._watcher === n &&
                    r._isMounted &&
                    !r._isDestroyed &&
                    gn(r, "updated");
                }
              })(r),
              re && V.devtools && re.emit("flush");
          }
          var jn = 0,
            Pn = function (e, t, n, r, a) {
              (this.vm = e),
                a && (e._watcher = this),
                e._watchers.push(this),
                r
                  ? ((this.deep = !!r.deep),
                    (this.user = !!r.user),
                    (this.lazy = !!r.lazy),
                    (this.sync = !!r.sync),
                    (this.before = r.before))
                  : (this.deep = this.user = this.lazy = this.sync = !1),
                (this.cb = n),
                (this.id = ++jn),
                (this.active = !0),
                (this.dirty = this.lazy),
                (this.deps = []),
                (this.newDeps = []),
                (this.depIds = new ie()),
                (this.newDepIds = new ie()),
                (this.expression = t.toString()),
                "function" == typeof t
                  ? (this.getter = t)
                  : ((this.getter = (function (e) {
                      if (!K.test(e)) {
                        var t = e.split(".");
                        return function (e) {
                          for (var n = 0; n < t.length; n++) {
                            if (!e) return;
                            e = e[t[n]];
                          }
                          return e;
                        };
                      }
                    })(t)),
                    this.getter ||
                      ((this.getter = I),
                      se(
                        'Failed watching path: "' +
                          t +
                          '" Watcher only accepts simple dot-delimited paths. For full control, use a function instead.',
                        e
                      ))),
                (this.value = this.lazy ? void 0 : this.get());
            };
          (Pn.prototype.get = function () {
            var e;
            he(this);
            var t = this.vm;
            try {
              e = this.getter.call(t, t);
            } catch (e) {
              if (!this.user) throw e;
              Ze(e, t, 'getter for watcher "' + this.expression + '"');
            } finally {
              this.deep && xt(e), me(), this.cleanupDeps();
            }
            return e;
          }),
            (Pn.prototype.addDep = function (e) {
              var t = e.id;
              this.newDepIds.has(t) ||
                (this.newDepIds.add(t),
                this.newDeps.push(e),
                this.depIds.has(t) || e.addSub(this));
            }),
            (Pn.prototype.cleanupDeps = function () {
              for (var e = this.deps.length; e--; ) {
                var t = this.deps[e];
                this.newDepIds.has(t.id) || t.removeSub(this);
              }
              var n = this.depIds;
              (this.depIds = this.newDepIds),
                (this.newDepIds = n),
                this.newDepIds.clear(),
                (n = this.deps),
                (this.deps = this.newDeps),
                (this.newDeps = n),
                (this.newDeps.length = 0);
            }),
            (Pn.prototype.update = function () {
              this.lazy
                ? (this.dirty = !0)
                : this.sync
                ? this.run()
                : (function (e) {
                    var t = e.id;
                    if (null == xn[t]) {
                      if (((xn[t] = !0), On)) {
                        for (var n = bn.length - 1; n > An && bn[n].id > e.id; )
                          n--;
                        bn.splice(n + 1, 0, e);
                      } else bn.push(e);
                      if (!Sn) {
                        if (((Sn = !0), !V.async)) return void En();
                        lt(En);
                      }
                    }
                  })(this);
            }),
            (Pn.prototype.run = function () {
              if (this.active) {
                var e = this.get();
                if (e !== this.value || c(e) || this.deep) {
                  var t = this.value;
                  if (((this.value = e), this.user))
                    try {
                      this.cb.call(this.vm, e, t);
                    } catch (e) {
                      Ze(
                        e,
                        this.vm,
                        'callback for watcher "' + this.expression + '"'
                      );
                    }
                  else this.cb.call(this.vm, e, t);
                }
              }
            }),
            (Pn.prototype.evaluate = function () {
              (this.value = this.get()), (this.dirty = !1);
            }),
            (Pn.prototype.depend = function () {
              for (var e = this.deps.length; e--; ) this.deps[e].depend();
            }),
            (Pn.prototype.teardown = function () {
              if (this.active) {
                this.vm._isBeingDestroyed || b(this.vm._watchers, this);
                for (var e = this.deps.length; e--; )
                  this.deps[e].removeSub(this);
                this.active = !1;
              }
            });
          var Cn = { enumerable: !0, configurable: !0, get: I, set: I };
          function In(e, t, n) {
            (Cn.get = function () {
              return this[t][n];
            }),
              (Cn.set = function (e) {
                this[t][n] = e;
              }),
              Object.defineProperty(e, n, Cn);
          }
          function Tn(t) {
            t._watchers = [];
            var n = t.$options;
            n.props &&
              (function (e, t) {
                var n = e.$options.propsData || {},
                  r = (e._props = {}),
                  a = (e.$options._propKeys = []),
                  i = !e.$parent;
                i || Ae(!1);
                var o = function (o) {
                  a.push(o);
                  var s = He(o, t, n, e),
                    c = $(o);
                  (g(c) || V.isReservedAttr(c)) &&
                    se(
                      '"' +
                        c +
                        '" is a reserved attribute and cannot be used as component prop.',
                      e
                    ),
                    je(r, o, s, function () {
                      if (!i && !hn) {
                        if (
                          "mp-baidu" === e.mpHost ||
                          "mp-kuaishou" === e.mpHost ||
                          "mp-xhs" === e.mpHost
                        )
                          return;
                        if (
                          "value" === o &&
                          Array.isArray(e.$options.behaviors) &&
                          -1 !==
                            e.$options.behaviors.indexOf("uni://form-field")
                        )
                          return;
                        if (e._getFormData) return;
                        for (var t = e.$parent; t; ) {
                          if (t.__next_tick_pending) return;
                          t = t.$parent;
                        }
                        se(
                          "Avoid mutating a prop directly since the value will be overwritten whenever the parent component re-renders. Instead, use a data or computed property based on the prop's value. Prop being mutated: \"" +
                            o +
                            '"',
                          e
                        );
                      }
                    }),
                    o in e || In(e, "_props", o);
                };
                for (var s in t) o(s);
                Ae(!0);
              })(t, n.props),
              n.methods &&
                (function (t, n) {
                  var r = t.$options.props;
                  for (var a in n)
                    "function" != typeof n[a] &&
                      se(
                        'Method "' +
                          a +
                          '" has type "' +
                          e(n[a]) +
                          '" in the component definition. Did you reference the function correctly?',
                        t
                      ),
                      r &&
                        x(r, a) &&
                        se(
                          'Method "' +
                            a +
                            '" has already been defined as a prop.',
                          t
                        ),
                      a in t &&
                        U(a) &&
                        se(
                          'Method "' +
                            a +
                            '" conflicts with an existing Vue instance method. Avoid defining component methods that start with _ or $.'
                        ),
                      (t[a] = "function" != typeof n[a] ? I : E(n[a], t));
                })(t, n.methods),
              n.data
                ? (function (e) {
                    var t = e.$options.data;
                    p(
                      (t = e._data =
                        "function" == typeof t
                          ? (function (e, t) {
                              he();
                              try {
                                return e.call(t, t);
                              } catch (e) {
                                return Ze(e, t, "data()"), {};
                              } finally {
                                me();
                              }
                            })(t, e)
                          : t || {})
                    ) ||
                      ((t = {}),
                      se(
                        "data functions should return an object:\nhttps://vuejs.org/v2/guide/components.html#data-Must-Be-a-Function",
                        e
                      ));
                    var n = Object.keys(t),
                      r = e.$options.props,
                      a = e.$options.methods,
                      i = n.length;
                    for (; i--; ) {
                      var o = n[i];
                      a &&
                        x(a, o) &&
                        se(
                          'Method "' +
                            o +
                            '" has already been defined as a data property.',
                          e
                        ),
                        r && x(r, o)
                          ? se(
                              'The data property "' +
                                o +
                                '" is already declared as a prop. Use prop default value instead.',
                              e
                            )
                          : U(o) || In(e, "_data", o);
                    }
                    Ee(t, !0);
                  })(t)
                : Ee((t._data = {}), !0),
              n.computed &&
                (function (e, t) {
                  var n = (e._computedWatchers = Object.create(null)),
                    r = ne();
                  for (var a in t) {
                    var i = t[a],
                      o = "function" == typeof i ? i : i.get;
                    null == o &&
                      se(
                        'Getter is missing for computed property "' + a + '".',
                        e
                      ),
                      r || (n[a] = new Pn(e, o || I, I, Ln)),
                      a in e
                        ? a in e.$data
                          ? se(
                              'The computed property "' +
                                a +
                                '" is already defined in data.',
                              e
                            )
                          : e.$options.props &&
                            a in e.$options.props &&
                            se(
                              'The computed property "' +
                                a +
                                '" is already defined as a prop.',
                              e
                            )
                        : Dn(e, a, i);
                  }
                })(t, n.computed),
              n.watch &&
                n.watch !== ee &&
                (function (e, t) {
                  for (var n in t) {
                    var r = t[n];
                    if (Array.isArray(r))
                      for (var a = 0; a < r.length; a++) Fn(e, n, r[a]);
                    else Fn(e, n, r);
                  }
                })(t, n.watch);
          }
          var Ln = { lazy: !0 };
          function Dn(e, t, n) {
            var r = !ne();
            "function" == typeof n
              ? ((Cn.get = r ? Mn(t) : Bn(n)), (Cn.set = I))
              : ((Cn.get = n.get
                  ? r && !1 !== n.cache
                    ? Mn(t)
                    : Bn(n.get)
                  : I),
                (Cn.set = n.set || I)),
              Cn.set === I &&
                (Cn.set = function () {
                  se(
                    'Computed property "' +
                      t +
                      '" was assigned to but it has no setter.',
                    this
                  );
                }),
              Object.defineProperty(e, t, Cn);
          }
          function Mn(e) {
            return function () {
              var t = this._computedWatchers && this._computedWatchers[e];
              if (t)
                return (
                  t.dirty && t.evaluate(),
                  _e.SharedObject.target && t.depend(),
                  t.value
                );
            };
          }
          function Bn(e) {
            return function () {
              return e.call(this, this);
            };
          }
          function Fn(e, t, n, r) {
            return (
              p(n) && ((r = n), (n = n.handler)),
              "string" == typeof n && (n = e[n]),
              e.$watch(t, n, r)
            );
          }
          var Nn = 0;
          function Vn(e) {
            var t = e.options;
            if (e.super) {
              var n = Vn(e.super);
              if (n !== e.superOptions) {
                e.superOptions = n;
                var r = (function (e) {
                  var t,
                    n = e.options,
                    r = e.sealedOptions;
                  for (var a in n)
                    n[a] !== r[a] && (t || (t = {}), (t[a] = n[a]));
                  return t;
                })(e);
                r && P(e.extendOptions, r),
                  (t = e.options = Re(n, e.extendOptions)).name &&
                    (t.components[t.name] = e);
              }
            }
            return t;
          }
          function Rn(e) {
            this instanceof Rn ||
              se(
                "Vue is a constructor and should be called with the `new` keyword"
              ),
              this._init(e);
          }
          function Un(e) {
            e.cid = 0;
            var t = 1;
            e.extend = function (e) {
              e = e || {};
              var n = this,
                r = n.cid,
                a = e._Ctor || (e._Ctor = {});
              if (a[r]) return a[r];
              var i = e.name || n.options.name;
              i && Ne(i);
              var o = function (e) {
                this._init(e);
              };
              return (
                ((o.prototype = Object.create(n.prototype)).constructor = o),
                (o.cid = t++),
                (o.options = Re(n.options, e)),
                (o.super = n),
                o.options.props &&
                  (function (e) {
                    var t = e.options.props;
                    for (var n in t) In(e.prototype, "_props", n);
                  })(o),
                o.options.computed &&
                  (function (e) {
                    var t = e.options.computed;
                    for (var n in t) Dn(e.prototype, n, t[n]);
                  })(o),
                (o.extend = n.extend),
                (o.mixin = n.mixin),
                (o.use = n.use),
                F.forEach(function (e) {
                  o[e] = n[e];
                }),
                i && (o.options.components[i] = o),
                (o.superOptions = n.options),
                (o.extendOptions = e),
                (o.sealedOptions = P({}, o.options)),
                (a[r] = o),
                o
              );
            };
          }
          function Hn(e) {
            return e && (e.Ctor.options.name || e.tag);
          }
          function Kn(e, t) {
            return Array.isArray(e)
              ? e.indexOf(t) > -1
              : "string" == typeof e
              ? e.split(",").indexOf(t) > -1
              : ((n = e), "[object RegExp]" === u.call(n) && e.test(t));
            var n;
          }
          function qn(e, t) {
            var n = e.cache,
              r = e.keys,
              a = e._vnode;
            for (var i in n) {
              var o = n[i];
              if (o) {
                var s = Hn(o.componentOptions);
                s && !t(s) && Gn(n, i, r, a);
              }
            }
          }
          function Gn(e, t, n, r) {
            var a = e[t];
            !a || (r && a.tag === r.tag) || a.componentInstance.$destroy(),
              (e[t] = null),
              b(n, t);
          }
          !(function (e) {
            e.prototype._init = function (e) {
              var t,
                n,
                a = this;
              (a._uid = Nn++),
                V.performance &&
                  gt &&
                  ((t = "vue-perf-start:" + a._uid),
                  (n = "vue-perf-end:" + a._uid),
                  gt(t)),
                (a._isVue = !0),
                e && e._isComponent
                  ? (function (e, t) {
                      var n = (e.$options = Object.create(
                          e.constructor.options
                        )),
                        r = t._parentVnode;
                      (n.parent = t.parent), (n._parentVnode = r);
                      var a = r.componentOptions;
                      (n.propsData = a.propsData),
                        (n._parentListeners = a.listeners),
                        (n._renderChildren = a.children),
                        (n._componentTag = a.tag),
                        t.render &&
                          ((n.render = t.render),
                          (n.staticRenderFns = t.staticRenderFns));
                    })(a, e)
                  : (a.$options = Re(Vn(a.constructor), e || {}, a)),
                nt(a),
                (a._self = a),
                (function (e) {
                  var t = e.$options,
                    n = t.parent;
                  if (n && !t.abstract) {
                    for (; n.$options.abstract && n.$parent; ) n = n.$parent;
                    n.$children.push(e);
                  }
                  (e.$parent = n),
                    (e.$root = n ? n.$root : e),
                    (e.$children = []),
                    (e.$refs = {}),
                    (e._watcher = null),
                    (e._inactive = null),
                    (e._directInactive = !1),
                    (e._isMounted = !1),
                    (e._isDestroyed = !1),
                    (e._isBeingDestroyed = !1);
                })(a),
                (function (e) {
                  (e._events = Object.create(null)), (e._hasHookEvent = !1);
                  var t = e.$options._parentListeners;
                  t && fn(e, t);
                })(a),
                (function (e) {
                  (e._vnode = null), (e._staticTrees = null);
                  var t = e.$options,
                    n = (e.$vnode = t._parentVnode),
                    a = n && n.context;
                  (e.$slots = It(t._renderChildren, a)),
                    (e.$scopedSlots = r),
                    (e._c = function (t, n, r, a) {
                      return on(e, t, n, r, a, !1);
                    }),
                    (e.$createElement = function (t, n, r, a) {
                      return on(e, t, n, r, a, !0);
                    });
                  var i = n && n.data;
                  je(
                    e,
                    "$attrs",
                    (i && i.attrs) || r,
                    function () {
                      !hn && se("$attrs is readonly.", e);
                    },
                    !0
                  ),
                    je(
                      e,
                      "$listeners",
                      t._parentListeners || r,
                      function () {
                        !hn && se("$listeners is readonly.", e);
                      },
                      !0
                    );
                })(a),
                gn(a, "beforeCreate"),
                !a._$fallback && Pt(a),
                Tn(a),
                !a._$fallback && jt(a),
                !a._$fallback && gn(a, "created"),
                V.performance &&
                  gt &&
                  ((a._name = le(a, !1)),
                  gt(n),
                  bt("vue " + a._name + " init", t, n)),
                a.$options.el && a.$mount(a.$options.el);
            };
          })(Rn),
            (function (e) {
              var t = {
                  get: function () {
                    return this._data;
                  },
                },
                n = {
                  get: function () {
                    return this._props;
                  },
                };
              (t.set = function () {
                se(
                  "Avoid replacing instance root $data. Use nested data properties instead.",
                  this
                );
              }),
                (n.set = function () {
                  se("$props is readonly.", this);
                }),
                Object.defineProperty(e.prototype, "$data", t),
                Object.defineProperty(e.prototype, "$props", n),
                (e.prototype.$set = Pe),
                (e.prototype.$delete = Ce),
                (e.prototype.$watch = function (e, t, n) {
                  if (p(t)) return Fn(this, e, t, n);
                  (n = n || {}).user = !0;
                  var r = new Pn(this, e, t, n);
                  if (n.immediate)
                    try {
                      t.call(this, r.value);
                    } catch (e) {
                      Ze(
                        e,
                        this,
                        'callback for immediate watcher "' + r.expression + '"'
                      );
                    }
                  return function () {
                    r.teardown();
                  };
                });
            })(Rn),
            (function (e) {
              var t = /^hook:/;
              (e.prototype.$on = function (e, n) {
                var r = this;
                if (Array.isArray(e))
                  for (var a = 0, i = e.length; a < i; a++) r.$on(e[a], n);
                else
                  (r._events[e] || (r._events[e] = [])).push(n),
                    t.test(e) && (r._hasHookEvent = !0);
                return r;
              }),
                (e.prototype.$once = function (e, t) {
                  var n = this;
                  function r() {
                    n.$off(e, r), t.apply(n, arguments);
                  }
                  return (r.fn = t), n.$on(e, r), n;
                }),
                (e.prototype.$off = function (e, t) {
                  var n = this;
                  if (!arguments.length)
                    return (n._events = Object.create(null)), n;
                  if (Array.isArray(e)) {
                    for (var r = 0, a = e.length; r < a; r++) n.$off(e[r], t);
                    return n;
                  }
                  var i,
                    o = n._events[e];
                  if (!o) return n;
                  if (!t) return (n._events[e] = null), n;
                  for (var s = o.length; s--; )
                    if ((i = o[s]) === t || i.fn === t) {
                      o.splice(s, 1);
                      break;
                    }
                  return n;
                }),
                (e.prototype.$emit = function (e) {
                  var t = this,
                    n = e.toLowerCase();
                  n !== e &&
                    t._events[n] &&
                    ce(
                      'Event "' +
                        n +
                        '" is emitted in component ' +
                        le(t) +
                        ' but the handler is registered for "' +
                        e +
                        '". Note that HTML attributes are case-insensitive and you cannot use v-on to listen to camelCase events when using in-DOM templates. You should probably use "' +
                        $(e) +
                        '" instead of "' +
                        e +
                        '".'
                    );
                  var r = t._events[e];
                  if (r) {
                    r = r.length > 1 ? j(r) : r;
                    for (
                      var a = j(arguments, 1),
                        i = 'event handler for "' + e + '"',
                        o = 0,
                        s = r.length;
                      o < s;
                      o++
                    )
                      Ye(r[o], t, a, t, i);
                  }
                  return t;
                });
            })(Rn),
            (function (e) {
              (e.prototype._update = function (e, t) {
                var n = this,
                  r = n.$el,
                  a = n._vnode,
                  i = (function (e) {
                    var t = _n;
                    return (
                      (_n = e),
                      function () {
                        _n = t;
                      }
                    );
                  })(n);
                (n._vnode = e),
                  (n.$el = a
                    ? n.__patch__(a, e)
                    : n.__patch__(n.$el, e, t, !1)),
                  i(),
                  r && (r.__vue__ = null),
                  n.$el && (n.$el.__vue__ = n),
                  n.$vnode &&
                    n.$parent &&
                    n.$vnode === n.$parent._vnode &&
                    (n.$parent.$el = n.$el);
              }),
                (e.prototype.$forceUpdate = function () {
                  this._watcher && this._watcher.update();
                }),
                (e.prototype.$destroy = function () {
                  var e = this;
                  if (!e._isBeingDestroyed) {
                    gn(e, "beforeDestroy"), (e._isBeingDestroyed = !0);
                    var t = e.$parent;
                    !t ||
                      t._isBeingDestroyed ||
                      e.$options.abstract ||
                      b(t.$children, e),
                      e._watcher && e._watcher.teardown();
                    for (var n = e._watchers.length; n--; )
                      e._watchers[n].teardown();
                    e._data.__ob__ && e._data.__ob__.vmCount--,
                      (e._isDestroyed = !0),
                      e.__patch__(e._vnode, null),
                      gn(e, "destroyed"),
                      e.$off(),
                      e.$el && (e.$el.__vue__ = null),
                      e.$vnode && (e.$vnode.parent = null);
                  }
                });
            })(Rn),
            (function (e) {
              Zt(e.prototype),
                (e.prototype.$nextTick = function (e) {
                  return lt(e, this);
                }),
                (e.prototype._render = function () {
                  var e,
                    t = this,
                    n = t.$options,
                    r = n.render,
                    a = n._parentVnode;
                  a &&
                    (t.$scopedSlots = Lt(
                      a.data.scopedSlots,
                      t.$slots,
                      t.$scopedSlots
                    )),
                    (t.$vnode = a);
                  try {
                    (cn = t), (e = r.call(t._renderProxy, t.$createElement));
                  } catch (n) {
                    if ((Ze(n, t, "render"), t.$options.renderError))
                      try {
                        e = t.$options.renderError.call(
                          t._renderProxy,
                          t.$createElement,
                          n
                        );
                      } catch (n) {
                        Ze(n, t, "renderError"), (e = t._vnode);
                      }
                    else e = t._vnode;
                  } finally {
                    cn = null;
                  }
                  return (
                    Array.isArray(e) && 1 === e.length && (e = e[0]),
                    e instanceof ve ||
                      (Array.isArray(e) &&
                        se(
                          "Multiple root nodes returned from render function. Render function should return a single root node.",
                          t
                        ),
                      (e = be())),
                    (e.parent = a),
                    e
                  );
                });
            })(Rn);
          var zn = [String, RegExp, Array],
            Jn = {
              KeepAlive: {
                name: "keep-alive",
                abstract: !0,
                props: { include: zn, exclude: zn, max: [String, Number] },
                created: function () {
                  (this.cache = Object.create(null)), (this.keys = []);
                },
                destroyed: function () {
                  for (var e in this.cache) Gn(this.cache, e, this.keys);
                },
                mounted: function () {
                  var e = this;
                  this.$watch("include", function (t) {
                    qn(e, function (e) {
                      return Kn(t, e);
                    });
                  }),
                    this.$watch("exclude", function (t) {
                      qn(e, function (e) {
                        return !Kn(t, e);
                      });
                    });
                },
                render: function () {
                  var e = this.$slots.default,
                    t = (function (e) {
                      if (Array.isArray(e))
                        for (var t = 0; t < e.length; t++) {
                          var n = e[t];
                          if (
                            i(n) &&
                            (i(n.componentOptions) ||
                              ((r = n).isComment && r.asyncFactory))
                          )
                            return n;
                        }
                      var r;
                    })(e),
                    n = t && t.componentOptions;
                  if (n) {
                    var r = Hn(n),
                      a = this.include,
                      o = this.exclude;
                    if ((a && (!r || !Kn(a, r))) || (o && r && Kn(o, r)))
                      return t;
                    var s = this.cache,
                      c = this.keys,
                      u =
                        null == t.key
                          ? n.Ctor.cid + (n.tag ? "::" + n.tag : "")
                          : t.key;
                    s[u]
                      ? ((t.componentInstance = s[u].componentInstance),
                        b(c, u),
                        c.push(u))
                      : ((s[u] = t),
                        c.push(u),
                        this.max &&
                          c.length > parseInt(this.max) &&
                          Gn(s, c[0], c, this._vnode)),
                      (t.data.keepAlive = !0);
                  }
                  return t || (e && e[0]);
                },
              },
            };
          !(function (e) {
            var t = {
              get: function () {
                return V;
              },
              set: function () {
                se(
                  "Do not replace the Vue.config object, set individual fields instead."
                );
              },
            };
            Object.defineProperty(e, "config", t),
              (e.util = {
                warn: se,
                extend: P,
                mergeOptions: Re,
                defineReactive: je,
              }),
              (e.set = Pe),
              (e.delete = Ce),
              (e.nextTick = lt),
              (e.observable = function (e) {
                return Ee(e), e;
              }),
              (e.options = Object.create(null)),
              F.forEach(function (t) {
                e.options[t + "s"] = Object.create(null);
              }),
              (e.options._base = e),
              P(e.options.components, Jn),
              (function (e) {
                e.use = function (e) {
                  var t =
                    this._installedPlugins || (this._installedPlugins = []);
                  if (t.indexOf(e) > -1) return this;
                  var n = j(arguments, 1);
                  return (
                    n.unshift(this),
                    "function" == typeof e.install
                      ? e.install.apply(e, n)
                      : "function" == typeof e && e.apply(null, n),
                    t.push(e),
                    this
                  );
                };
              })(e),
              (function (e) {
                e.mixin = function (e) {
                  return (this.options = Re(this.options, e)), this;
                };
              })(e),
              Un(e),
              (function (e) {
                F.forEach(function (t) {
                  e[t] = function (e, n) {
                    return n
                      ? ("component" === t && Ne(e),
                        "component" === t &&
                          p(n) &&
                          ((n.name = n.name || e),
                          (n = this.options._base.extend(n))),
                        "directive" === t &&
                          "function" == typeof n &&
                          (n = { bind: n, update: n }),
                        (this.options[t + "s"][e] = n),
                        n)
                      : this.options[t + "s"][e];
                  };
                });
              })(e);
          })(Rn),
            Object.defineProperty(Rn.prototype, "$isServer", { get: ne }),
            Object.defineProperty(Rn.prototype, "$ssrContext", {
              get: function () {
                return this.$vnode && this.$vnode.ssrContext;
              },
            }),
            Object.defineProperty(Rn, "FunctionalRenderContext", { value: Yt }),
            (Rn.version = "2.6.11");
          var Wn = "[object Array]",
            Xn = "[object Object]";
          function Zn(e, t) {
            var n = {};
            return (
              (function e(t, n) {
                if (t === n) return;
                var r = Qn(t),
                  a = Qn(n);
                if (r == Xn && a == Xn) {
                  if (Object.keys(t).length >= Object.keys(n).length)
                    for (var i in n) {
                      var o = t[i];
                      void 0 === o ? (t[i] = null) : e(o, n[i]);
                    }
                } else
                  r == Wn &&
                    a == Wn &&
                    t.length >= n.length &&
                    n.forEach(function (n, r) {
                      e(t[r], n);
                    });
              })(e, t),
              (function e(t, n, r, a) {
                if (t === n) return;
                var i = Qn(t),
                  o = Qn(n);
                if (i == Xn)
                  if (o != Xn || Object.keys(t).length < Object.keys(n).length)
                    Yn(a, r, t);
                  else {
                    var s = function (i) {
                      var o = t[i],
                        s = n[i],
                        c = Qn(o),
                        u = Qn(s);
                      if (c != Wn && c != Xn)
                        o !== n[i] &&
                          (function (e, t) {
                            if (
                              !(
                                ("[object Null]" !== e &&
                                  "[object Undefined]" !== e) ||
                                ("[object Null]" !== t &&
                                  "[object Undefined]" !== t)
                              )
                            )
                              return !1;
                            return !0;
                          })(c, u) &&
                          Yn(a, ("" == r ? "" : r + ".") + i, o);
                      else if (c == Wn)
                        u != Wn || o.length < s.length
                          ? Yn(a, ("" == r ? "" : r + ".") + i, o)
                          : o.forEach(function (t, n) {
                              e(
                                t,
                                s[n],
                                ("" == r ? "" : r + ".") + i + "[" + n + "]",
                                a
                              );
                            });
                      else if (c == Xn)
                        if (
                          u != Xn ||
                          Object.keys(o).length < Object.keys(s).length
                        )
                          Yn(a, ("" == r ? "" : r + ".") + i, o);
                        else
                          for (var l in o)
                            e(
                              o[l],
                              s[l],
                              ("" == r ? "" : r + ".") + i + "." + l,
                              a
                            );
                    };
                    for (var c in t) s(c);
                  }
                else
                  i == Wn
                    ? o != Wn || t.length < n.length
                      ? Yn(a, r, t)
                      : t.forEach(function (t, i) {
                          e(t, n[i], r + "[" + i + "]", a);
                        })
                    : Yn(a, r, t);
              })(e, t, "", n),
              n
            );
          }
          function Yn(e, t, n) {
            e[t] = n;
          }
          function Qn(e) {
            return Object.prototype.toString.call(e);
          }
          function er(e) {
            if (e.__next_tick_callbacks && e.__next_tick_callbacks.length) {
              if (
                Object({
                  NODE_ENV: "development",
                  VUE_APP_DARK_MODE: "false",
                  VUE_APP_NAME: "GEKOO",
                  VUE_APP_PLATFORM: "mp-weixin",
                  BASE_URL: "/",
                }).VUE_APP_DEBUG
              ) {
                var t = e.$scope;
                console.log(
                  "[" +
                    +new Date() +
                    "][" +
                    (t.is || t.route) +
                    "][" +
                    e._uid +
                    "]:flushCallbacks[" +
                    e.__next_tick_callbacks.length +
                    "]"
                );
              }
              var n = e.__next_tick_callbacks.slice(0);
              e.__next_tick_callbacks.length = 0;
              for (var r = 0; r < n.length; r++) n[r]();
            }
          }
          function tr(e, t) {
            if (
              !e.__next_tick_pending &&
              !(function (e) {
                return bn.find(function (t) {
                  return e._watcher === t;
                });
              })(e)
            ) {
              if (
                Object({
                  NODE_ENV: "development",
                  VUE_APP_DARK_MODE: "false",
                  VUE_APP_NAME: "GEKOO",
                  VUE_APP_PLATFORM: "mp-weixin",
                  BASE_URL: "/",
                }).VUE_APP_DEBUG
              ) {
                var n = e.$scope;
                console.log(
                  "[" +
                    +new Date() +
                    "][" +
                    (n.is || n.route) +
                    "][" +
                    e._uid +
                    "]:nextVueTick"
                );
              }
              return lt(t, e);
            }
            if (
              Object({
                NODE_ENV: "development",
                VUE_APP_DARK_MODE: "false",
                VUE_APP_NAME: "GEKOO",
                VUE_APP_PLATFORM: "mp-weixin",
                BASE_URL: "/",
              }).VUE_APP_DEBUG
            ) {
              var r = e.$scope;
              console.log(
                "[" +
                  +new Date() +
                  "][" +
                  (r.is || r.route) +
                  "][" +
                  e._uid +
                  "]:nextMPTick"
              );
            }
            var a;
            if (
              (e.__next_tick_callbacks || (e.__next_tick_callbacks = []),
              e.__next_tick_callbacks.push(function () {
                if (t)
                  try {
                    t.call(e);
                  } catch (t) {
                    Ze(t, e, "nextTick");
                  }
                else a && a(e);
              }),
              !t && "undefined" != typeof Promise)
            )
              return new Promise(function (e) {
                a = e;
              });
          }
          function nr(e, t) {
            return t && (t._isVue || t.__v_isMPComponent) ? {} : t;
          }
          function rr() {}
          function ar(e) {
            return Array.isArray(e)
              ? (function (e) {
                  for (var t, n = "", r = 0, a = e.length; r < a; r++)
                    i((t = ar(e[r]))) &&
                      "" !== t &&
                      (n && (n += " "), (n += t));
                  return n;
                })(e)
              : c(e)
              ? (function (e) {
                  var t = "";
                  for (var n in e) e[n] && (t && (t += " "), (t += n));
                  return t;
                })(e)
              : "string" == typeof e
              ? e
              : "";
          }
          var ir = w(function (e) {
            var t = {},
              n = /:(.+)/;
            return (
              e.split(/;(?![^(]*\))/g).forEach(function (e) {
                if (e) {
                  var r = e.split(n);
                  r.length > 1 && (t[r[0].trim()] = r[1].trim());
                }
              }),
              t
            );
          });
          var or = [
            "createSelectorQuery",
            "createIntersectionObserver",
            "selectAllComponents",
            "selectComponent",
          ];
          var sr = [
            "onLaunch",
            "onShow",
            "onHide",
            "onUniNViewMessage",
            "onPageNotFound",
            "onThemeChange",
            "onError",
            "onUnhandledRejection",
            "onInit",
            "onLoad",
            "onReady",
            "onUnload",
            "onPullDownRefresh",
            "onReachBottom",
            "onTabItemTap",
            "onAddToFavorites",
            "onShareTimeline",
            "onShareAppMessage",
            "onResize",
            "onPageScroll",
            "onNavigationBarButtonTap",
            "onBackPress",
            "onNavigationBarSearchInputChanged",
            "onNavigationBarSearchInputConfirmed",
            "onNavigationBarSearchInputClicked",
            "onUploadDouyinVideo",
            "onNFCReadMessage",
            "onPageShow",
            "onPageHide",
            "onPageResize",
          ];
          (Rn.prototype.__patch__ = function (e, t) {
            var n = this;
            if (
              null !== t &&
              ("page" === this.mpType || "component" === this.mpType)
            ) {
              var r = this.$scope,
                a = Object.create(null);
              try {
                a = (function (e) {
                  var t = Object.create(null);
                  []
                    .concat(
                      Object.keys(e._data || {}),
                      Object.keys(e._computedWatchers || {})
                    )
                    .reduce(function (t, n) {
                      return (t[n] = e[n]), t;
                    }, t);
                  var n = e.__composition_api_state__ || e.__secret_vfa_state__,
                    r = n && n.rawBindings;
                  return (
                    r &&
                      Object.keys(r).forEach(function (n) {
                        t[n] = e[n];
                      }),
                    Object.assign(t, e.$mp.data || {}),
                    Array.isArray(e.$options.behaviors) &&
                      -1 !== e.$options.behaviors.indexOf("uni://form-field") &&
                      ((t.name = e.name), (t.value = e.value)),
                    JSON.parse(JSON.stringify(t, nr))
                  );
                })(this);
              } catch (e) {
                console.error(e);
              }
              a.__webviewId__ = r.data.__webviewId__;
              var i = Object.create(null);
              Object.keys(a).forEach(function (e) {
                i[e] = r.data[e];
              });
              var o = !1 === this.$shouldDiffData ? a : Zn(a, i);
              Object.keys(o).length
                ? (Object({
                    NODE_ENV: "development",
                    VUE_APP_DARK_MODE: "false",
                    VUE_APP_NAME: "GEKOO",
                    VUE_APP_PLATFORM: "mp-weixin",
                    BASE_URL: "/",
                  }).VUE_APP_DEBUG &&
                    console.log(
                      "[" +
                        +new Date() +
                        "][" +
                        (r.is || r.route) +
                        "][" +
                        this._uid +
                        "]差量更新",
                      JSON.stringify(o)
                    ),
                  (this.__next_tick_pending = !0),
                  r.setData(o, function () {
                    (n.__next_tick_pending = !1), er(n);
                  }))
                : er(this);
            }
          }),
            (Rn.prototype.$mount = function (e, t) {
              return (function (e, t, n) {
                return e.mpType
                  ? ("app" === e.mpType && (e.$options.render = rr),
                    e.$options.render ||
                      ((e.$options.render = rr),
                      (e.$options.template &&
                        "#" !== e.$options.template.charAt(0)) ||
                      e.$options.el ||
                      t
                        ? se(
                            "You are using the runtime-only build of Vue where the template compiler is not available. Either pre-compile the templates into render functions, or use the compiler-included build.",
                            e
                          )
                        : se(
                            "Failed to mount component: template or render function not defined.",
                            e
                          )),
                    !e._$fallback && gn(e, "beforeMount"),
                    new Pn(
                      e,
                      function () {
                        e._update(e._render(), n);
                      },
                      I,
                      {
                        before: function () {
                          e._isMounted &&
                            !e._isDestroyed &&
                            gn(e, "beforeUpdate");
                        },
                      },
                      !0
                    ),
                    (n = !1),
                    e)
                  : e;
              })(this, e, t);
            }),
            (function (e) {
              var t = e.extend;
              e.extend = function (e) {
                var n = (e = e || {}).methods;
                return (
                  n &&
                    Object.keys(n).forEach(function (t) {
                      -1 !== sr.indexOf(t) && ((e[t] = n[t]), delete n[t]);
                    }),
                  t.call(this, e)
                );
              };
              var n = e.config.optionMergeStrategies,
                r = n.created;
              sr.forEach(function (e) {
                n[e] = r;
              }),
                (e.prototype.__lifecycle_hooks__ = sr);
            })(Rn),
            (function (e) {
              e.config.errorHandler = function (t, n, r) {
                e.util.warn("Error in " + r + ': "' + t.toString() + '"', n),
                  console.error(t);
                var a = "function" == typeof getApp && getApp();
                a && a.onError && a.onError(t);
              };
              var t = e.prototype.$emit;
              (e.prototype.$emit = function (e) {
                if (this.$scope && e) {
                  var n = this.$scope._triggerEvent || this.$scope.triggerEvent;
                  if (n)
                    try {
                      n.call(this.$scope, e, { __args__: j(arguments, 1) });
                    } catch (e) {}
                }
                return t.apply(this, arguments);
              }),
                (e.prototype.$nextTick = function (e) {
                  return tr(this, e);
                }),
                or.forEach(function (t) {
                  e.prototype[t] = function (e) {
                    return this.$scope && this.$scope[t]
                      ? this.$scope[t](e)
                      : "undefined" != typeof my
                      ? "createSelectorQuery" === t
                        ? my.createSelectorQuery(e)
                        : "createIntersectionObserver" === t
                        ? my.createIntersectionObserver(e)
                        : void 0
                      : void 0;
                  };
                }),
                (e.prototype.__init_provide = jt),
                (e.prototype.__init_injections = Pt),
                (e.prototype.__call_hook = function (e, t) {
                  var n = this;
                  he();
                  var r,
                    a = n.$options[e],
                    i = e + " hook";
                  if (a)
                    for (var o = 0, s = a.length; o < s; o++)
                      r = Ye(a[o], n, t ? [t] : null, n, i);
                  return n._hasHookEvent && n.$emit("hook:" + e, t), me(), r;
                }),
                (e.prototype.__set_model = function (t, n, r, a) {
                  Array.isArray(a) &&
                    (-1 !== a.indexOf("trim") && (r = r.trim()),
                    -1 !== a.indexOf("number") && (r = this._n(r))),
                    t || (t = this),
                    e.set(t, n, r);
                }),
                (e.prototype.__set_sync = function (t, n, r) {
                  t || (t = this), e.set(t, n, r);
                }),
                (e.prototype.__get_orig = function (e) {
                  return (p(e) && e.$orig) || e;
                }),
                (e.prototype.__get_value = function (e, t) {
                  return (function e(t, n) {
                    var r = n.split("."),
                      a = r[0];
                    return (
                      0 === a.indexOf("__$n") &&
                        (a = parseInt(a.replace("__$n", ""))),
                      1 === r.length ? t[a] : e(t[a], r.slice(1).join("."))
                    );
                  })(t || this, e);
                }),
                (e.prototype.__get_class = function (e, t) {
                  return (function (e, t) {
                    return i(e) || i(t)
                      ? ((n = e),
                        (r = ar(t)),
                        n ? (r ? n + " " + r : n) : r || "")
                      : "";
                    var n, r;
                  })(t, e);
                }),
                (e.prototype.__get_style = function (e, t) {
                  if (!e && !t) return "";
                  var n,
                    r =
                      ((n = e),
                      Array.isArray(n)
                        ? C(n)
                        : "string" == typeof n
                        ? ir(n)
                        : n),
                    a = t ? P(t, r) : r;
                  return Object.keys(a)
                    .map(function (e) {
                      return $(e) + ":" + a[e];
                    })
                    .join(";");
                }),
                (e.prototype.__map = function (e, t) {
                  var n, r, a, i, o;
                  if (Array.isArray(e)) {
                    for (
                      n = new Array(e.length), r = 0, a = e.length;
                      r < a;
                      r++
                    )
                      n[r] = t(e[r], r);
                    return n;
                  }
                  if (c(e)) {
                    for (
                      i = Object.keys(e),
                        n = Object.create(null),
                        r = 0,
                        a = i.length;
                      r < a;
                      r++
                    )
                      n[(o = i[r])] = t(e[o], o, r);
                    return n;
                  }
                  if ("number" == typeof e) {
                    for (n = new Array(e), r = 0, a = e; r < a; r++)
                      n[r] = t(r, r);
                    return n;
                  }
                  return [];
                });
            })(Rn),
            (n.default = Rn);
        }.call(this, r(/*! ./../../../../../webpack/buildin/global.js */ 3));
    },
    /*!*********************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/pages.json ***!
  \*********************************************************/
    /*! no static exports found */ function (e, t) {},
    ,
    ,
    ,
    ,
    ,
    /*!**********************************************************************************************************!*\
  !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js ***!
  \**********************************************************************************************************/
    /*! exports provided: default */ function (e, t, n) {
      function r(e, t, n, r, a, i, o, s, c, u) {
        var l,
          p = "function" == typeof e ? e.options : e;
        if (c) {
          p.components || (p.components = {});
          var d = Object.prototype.hasOwnProperty;
          for (var f in c)
            d.call(c, f) &&
              !d.call(p.components, f) &&
              (p.components[f] = c[f]);
        }
        if (
          (u &&
            ("function" == typeof u.beforeCreate &&
              (u.beforeCreate = [u.beforeCreate]),
            (u.beforeCreate || (u.beforeCreate = [])).unshift(function () {
              this[u.__module] = this;
            }),
            (p.mixins || (p.mixins = [])).push(u)),
          t && ((p.render = t), (p.staticRenderFns = n), (p._compiled = !0)),
          r && (p.functional = !0),
          i && (p._scopeId = "data-v-" + i),
          o
            ? ((l = function (e) {
                (e =
                  e ||
                  (this.$vnode && this.$vnode.ssrContext) ||
                  (this.parent &&
                    this.parent.$vnode &&
                    this.parent.$vnode.ssrContext)) ||
                  "undefined" == typeof __VUE_SSR_CONTEXT__ ||
                  (e = __VUE_SSR_CONTEXT__),
                  a && a.call(this, e),
                  e &&
                    e._registeredComponents &&
                    e._registeredComponents.add(o);
              }),
              (p._ssrRegister = l))
            : a &&
              (l = s
                ? function () {
                    a.call(this, this.$root.$options.shadowRoot);
                  }
                : a),
          l)
        )
          if (p.functional) {
            p._injectStyles = l;
            var _ = p.render;
            p.render = function (e, t) {
              return l.call(t), _(e, t);
            };
          } else {
            var h = p.beforeCreate;
            p.beforeCreate = h ? [].concat(h, l) : [l];
          }
        return { exports: e, options: p };
      }
      n.r(t),
        n.d(t, "default", function () {
          return r;
        });
    },
    /*!***********************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/uni.promisify.adaptor.js ***!
  \***********************************************************************/
    /*! no static exports found */ function (e, t, n) {
      (function (e) {
        var t = n(/*! @babel/runtime/helpers/typeof */ 13);
        e.addInterceptor({
          returnValue: function (e) {
            return !e ||
              ("object" !== t(e) && "function" != typeof e) ||
              "function" != typeof e.then
              ? e
              : new Promise(function (t, n) {
                  e.then(function (e) {
                    return e ? (e[0] ? n(e[0]) : t(e[1])) : t(e);
                  });
                });
          },
        });
      }).call(
        this,
        n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2).default
      );
    },
    ,
    ,
    ,
    ,
    ,
    ,
    /*!************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/utils/i18n.js ***!
  \************************************************************/
    /*! no static exports found */ function (e, t, n) {
      (function (e) {
        var r = n(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
        Object.defineProperty(t, "__esModule", { value: !0 }),
          (t.getLocale = function () {
            return p;
          }),
          (t.setLocale = function (t) {
            if (t != p) {
              (p = t), e.setStorageSync("locale", t);
              var n =
                "zh" === p
                  ? "语言已切换为中文"
                  : "Language has been switched to English";
              e.showToast({ title: n, icon: "none", duration: 2e3 });
            }
          }),
          (t.t = function (e) {
            var t,
              n = e.split("."),
              r = l[p],
              i = (function (e, t) {
                var n =
                  ("undefined" != typeof Symbol && e[Symbol.iterator]) ||
                  e["@@iterator"];
                if (!n) {
                  if (
                    Array.isArray(e) ||
                    (n = (function (e, t) {
                      if (!e) return;
                      if ("string" == typeof e) return u(e, t);
                      var n = Object.prototype.toString.call(e).slice(8, -1);
                      "Object" === n &&
                        e.constructor &&
                        (n = e.constructor.name);
                      if ("Map" === n || "Set" === n) return Array.from(e);
                      if (
                        "Arguments" === n ||
                        /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)
                      )
                        return u(e, t);
                    })(e)) ||
                    (t && e && "number" == typeof e.length)
                  ) {
                    n && (e = n);
                    var r = 0,
                      a = function () {};
                    return {
                      s: a,
                      n: function () {
                        return r >= e.length
                          ? { done: !0 }
                          : { done: !1, value: e[r++] };
                      },
                      e: function (e) {
                        throw e;
                      },
                      f: a,
                    };
                  }
                  throw new TypeError(
                    "Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."
                  );
                }
                var i,
                  o = !0,
                  s = !1;
                return {
                  s: function () {
                    n = n.call(e);
                  },
                  n: function () {
                    var e = n.next();
                    return (o = e.done), e;
                  },
                  e: function (e) {
                    (s = !0), (i = e);
                  },
                  f: function () {
                    try {
                      o || null == n.return || n.return();
                    } finally {
                      if (s) throw i;
                    }
                  },
                };
              })(n);
            try {
              for (i.s(); !(t = i.n()).done; ) {
                var o = t.value;
                if (!r || "object" !== (0, a.default)(r) || !(o in r)) return e;
                r = r[o];
              }
            } catch (e) {
              i.e(e);
            } finally {
              i.f();
            }
            return r;
          });
        var a = r(n(/*! @babel/runtime/helpers/typeof */ 13)),
          i = r(n(/*! @/locales/en.json */ 41)),
          o = r(n(/*! @/locales/zh.json */ 42)),
          s = r(n(/*! @/locales/zhft.json */ 43)),
          c = r(n(/*! @/locales/japanese.json */ 44));
        function u(e, t) {
          (null == t || t > e.length) && (t = e.length);
          for (var n = 0, r = new Array(t); n < t; n++) r[n] = e[n];
          return r;
        }
        var l = {
            en: i.default,
            zh: o.default,
            zhft: s.default,
            japanese: c.default,
          },
          p = e.getStorageSync("locale") || "zh";
      }).call(
        this,
        n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2).default
      );
    },
    /*!**************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/locales/en.json ***!
  \**************************************************************/
    /*! exports provided: componentName, welcomeMessage, welcome, message, home_page, ble_msg, ble_page, contact, faq_page, lang_page, default */ function (
      e
    ) {
      e.exports = JSON.parse(
        '{"componentName":"Component Name","welcomeMessage":"Welcome to our app","welcome":"Welcome to use","message":"GEKOO Controller","home_page":{"name":"GEKOO","grid":{"item0":"Bluetooth Assistant","item1":"Teaching Videos","item2":"Contact Us","item3":"Frequently Asked Questions","item4":"Product Introduction","item5":"Software Version","item6":"Firmware Version","item7":"Language Switch","item8":"Reserve"}},"ble_msg":{"error":{"title":"Bluetooth Error","content1":"No Bluetooth permission. Please check the Bluetooth permissions of WeChat and the mini - program.","content2":"Bluetooth is off. Please turn it on and try again."},"search":{"title":"Search Failed","content":"Bluetooth search failed. Please check if the Bluetooth function is working properly.","content1":"The specified Bluetooth device was not found. Please ensure the device is turned on and nearby."},"conn":{"title":"Connection Failed","content":"Bluetooth connection failed. Please check the device or try again."},"discon":{"title":"Connection Disconnected","content":"The Bluetooth connection has been disconnected. Please reconnect."}},"ble_page":{"name":"BLE Tool","tabbar":{"realtime":"Realtime","parameter":"Parameter","firmwave":"Firmware","assistant":"Assistant"},"ble":{"link":"Connected","unlink":"Disconnected"},"card1":{"vol":"Voltage","cur":"Current","velo":"Velocity","temp":"Temperature","vain":"VAin","error":"Err Info","btn_learn":"Auto Adaptive","btn_check":"Check Key","btn_error":"Error Info","btn_switch":"Switch Display","btn_warn":"Security","btn_level":"Reconnect BT"},"identity":{"freshman":"Newcomer - Simplified Parameters","expert":"Expert Tuning - All Parameters","flowchart":"Parameter Tuning Flowchart","parametertip":"Parameter Tip Switch"},"tip":"Before replacing the motor, controller, and activating the forced lock motor, hill stop, and TCS functions, automatic adaptation must be performed！","para_title":{"cur_group":"Current Group","cur_group_tip":"Directly affects controller output","motor_group":"Motor Group","motor_group_tip":"Motor-related parameters","handle_group":"Throttle Group","handle_group_tip":"Throttle effective range settings","start_group":"Startup Acceleration","start_group_tip":"Startup and function switches","park_group":"Park Mode","park_group_tip":"Park mode adaptation","appearance_group":"Vehicle/Display","appearance_group_tip":"Display/protocol/vehicle","threespeed_group":"Three-Speed","threespeed_group_tip":"Speed and power for different gears","rev_group":"Reverse","rev_group_tip":"Assistive reverse power settings","voltage_group":"Voltage Settings","voltage_group_tip":"Battery voltage adaptation","soc_group":"SOC","soc_group_tip":"Estimate remaining battery","ebs_group":"EBS Settings","ebs_group_tip":"EBS strength and effective speed range","advanced_group":"Advanced Settings","advanced_group_tip":"Generally not needed for adjustments"},"operationpanel":"Operation panel","actionsheet":{"readpara":"Read Parameter","writepara":"Write Parameter","factory":"Restor Factory","identify":"Parameter Assistant","blekey":"Ble Password","manual":"Switch Battery","parametertip":"Parameter Tip"},"parameter":{"bus_current":"IBus max","phase_line_current":"IPhase max","motor_direction":"Direction","position_sensor_type":"Sensor type","hall_sequence":"Hall sequence","encoder_sequence":"Encoder sequence","encoder_accuracy":"Encoder accuracy","phase_shift_angle":"Phase shift angle","pole_pairs":"Pole pairs","full_throttle_voltage":"Full throttle voltage","idle_throttle_voltage":"Idle throttle voltage","forward_gear":"Soft start time","reverse_gear":"Soft start time","anti_theft_lock_motor":"Anti-theft lock motor","lock_motor_mode":"Lock motor mode","auto_parking":"Auto parking","parking_signal":"Parking signal","side_stand_effective":"Side stand effective","brake_driver":"Brake power off ","riding_mode":"Riding mode","signal_port_selection":"Signal port selection","trigger_mode":"Trigger mode","conventional_brake_release_p":"Conventional brake release P","one_line_coefficient":"One-line coefficient","hall_meter_pulse_count":"Hall meter pulse count","bms_protocol":"BMS Protocol","high_speed_gear_max_speed":"H:max speed","high_speed_gear_phase_line_current":"H:phase current","high_speed_gear_bus_current":"H:bus current","high_speed_gear_weak_magnetic_current":"H:weak magnetic current","medium_speed_gear_max_speed":"M:max speed","medium_speed_gear_phase_line_current":"M:phase line current","medium_speed_gear_bus_current":"M:bus current","low_speed_gear_max_speed":"L:max speed","low_speed_gear_phase_line_current":"L:phase line current","low_speed_gear_bus_current":"L:bus current","shift_method":"Shift method","momentary_default_gear":"Momentary default gear","reverse_speed":"Reverse speed","reverse_torque":"Reverse torque","under_voltage_point":"Under-voltage","over_voltage_point":"Over-voltage","power_reduction_start_voltage":"Power reduction start voltage","power_reduction_end_voltage":"Power reduction end voltage","battery_series":"Battery series","battery_capacity":"Battery capacity","discharge_coefficient":"Discharge coefficient","battery_type":"Battery type","brake_feedback_torque":"Brake feedback torque","release_throttle_feedback_torque":"Release throttle feedback torque","regenerative_current":"Regenerative current","minimum_feedback_speed":"Minimum feedback speed","regenerative_voltage_limit":"Regenerative voltage limit","model_selection":"Model selection","long_press_cruise_to_reverse":"Long press cruise to reverse","warning_function":"Warning function","C_Kp":"C_Kp","C_Ki":"C_Ki","S_Kp":"S_Kp","S_Ki":"S_Ki","LF_parameter":"LF parameter","SF_parameter":"SF parameter","utilization":"Utilization","margin":"Margin","adjustment_coefficient":"Adjustment coefficient","back_EMF":"Back EMF","rated_speed":"Rated speed","carrier_frequency":"Carrier frequency","reference_current":"Reference current","reference_1":"Reference 1","reference_2":"Reference 2","TCS_sensitivity":"TCS sensitivity"},"choice":{"forward":"Forward","reverse":"Reverse","positiveSequence":"Positive","reverseSequence":"Reverse","linear":"Linear","softStart":"Soft","motion":"Motion","violent":"Violent","open":"ON","close":"OFF","force":"Force","energySaving":"Energy","lowBrake":"Lbrake","highBrake":"Hbrake","invalid":"Invalid","toggle":"Toggle","jog":"Jog","highSpeedGear":"High","mediumSpeedGear":"Medium","lowSpeedGear":"Low","ternaryLithium":"NMC","lithiumIronPhosphate":"LFP","leadAcidBattery":"VRLA","lithiumManganate":"LMO","default":"Default","cruise":"Cruise","addsub":"+&-"}},"contact":{"companyName":"Suzhou Zhiqi Drive Technology Co., Ltd.","address":"No.9 Longshan South Road,Guangfu Town,Wuzhong, Suzhou,Jiangsu","phone":"0512-66919004","email":"info@example.com","button1":"Reminder","button2":"Working hours","button3":"AfterSale Query"},"faq_page":{"area0":"故障解析","area1":"电机相关","area2":"自学习相关","area3":"仪表相关","area4":"车型相关","area5":"转把相关"},"lang_page":{"tip":"You can contact the manufacturer to customize language support for other countries."}}'
      );
    },
    /*!**************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/locales/zh.json ***!
  \**************************************************************/
    /*! exports provided: componentName, welcomeMessage, welcome, message, home_page, ble_msg, ble_page, contact, faq_page, lang_page, default */ function (
      e
    ) {
      e.exports = JSON.parse(
        '{"componentName":"组件名称","welcomeMessage":"欢迎使用我们的应用","welcome":"欢迎使用","message":"智科控制器","home_page":{"name":"智科控制器","grid":{"item0":"蓝牙助手","item1":"新手教学","item2":"联系我们","item3":"常见问题","item4":"产品介绍","item5":"软件版本","item6":"固件版本","item7":"语言切换","item8":"售后查询"}},"ble_msg":{"error":{"title":"蓝牙异常","content1":"没有蓝牙权限，请检查微信和小程序的蓝牙权限","content2":"蓝牙功能没打开，请打开后重试。"},"search":{"title":"搜索失败","content":"蓝牙搜索失败，请检查蓝牙功能是否正常。","content1":"未搜索到指定蓝牙设备，请确保设备已开启并在附近。"},"conn":{"title":"连接失败","content":"蓝牙连接失败，请检查设备或重新尝试。"},"discon":{"title":"连接断开","content":"蓝牙连接已断开，请重新连接。"}},"ble_page":{"name":"蓝牙助手","tabbar":{"realtime":"实时监控","parameter":"修改参数","firmwave":"固件升级","assistant":"辅助工具"},"ble":{"link":"已连接","unlink":"未连接"},"card1":{"vol":"母线电压","cur":"母线电流","velo":"电机转速","temp":"控制器温度","vain":"转把电压","error":"故障信息","btn_learn":"自学习","btn_check":"蓝牙密码","btn_error":"故障解析","btn_switch":"切换显示","btn_warn":"其他功能","btn_level":"重连蓝牙"},"identity":{"freshman":"萌新调参-简化参数","expert":"专家调参-全部参数","flowchart":"调参流程图","parametertip":"参数提示开关"},"tip":"更换电机，控制器以及开启强制锁电机，驻坡和TCS功能一定要自学习！","para_title":{"cur_group":"电流组","cur_group_tip":"直接影响控制器的动力输出","motor_group":"电机组","motor_group_tip":"电机本身以及学习电流和弱磁电流的相关参数","handle_group":"转把和起步性能","handle_group_tip":"转把有效区间以及动力响应相关","start_group":"功能开关","start_group_tip":"常用功能选择是否打开","park_group":"P挡相关","park_group_tip":"驻车挡的适配","appearance_group":"车型/仪表","appearance_group_tip":"仪表/协议/车型","threespeed_group":"三速","threespeed_group_tip":"不同挡位的速度和动力","rev_group":"倒车","rev_group_tip":"助力推行为倒车设置的一半","voltage_group":"电压相关","voltage_group_tip":"适配电池电压参数","soc_group":"SOC相关","soc_group_tip":"部分没有计量模块的车型的SOC由控制器估算","ebs_group":"EBS相关","ebs_group_tip":"动能回收和陡坡缓降相关设置","advanced_group":"高级参数","advanced_group_tip":"一般不需要调整"},"operationpanel":"操作面板(读写参数)","actionsheet":{"readpara":"读取参数","writepara":"写入参数","factory":"恢复出厂","identify":"辅助调参","blekey":"修改蓝牙密码","manual":"一键换电","parametertip":"调参提示信息"},"parameter":{"bus_current":"母线电流","phase_line_current":"相线电流","motor_direction":"电机方向","position_sensor_type":"位置传感器类型","hall_sequence":"霍尔相序","encoder_sequence":"编码器相序","encoder_accuracy":"编码器精度","phase_shift_angle":"相移角度","pole_pairs":"极对数","full_throttle_voltage":"满转把电压","idle_throttle_voltage":"空转把电压","forward_gear":"D挡软启时间","reverse_gear":"R挡软启时间","anti_theft_lock_motor":"防盗锁电机","lock_motor_mode":"锁电机模式","auto_parking":"H坡道驻车","parking_signal":"驻车信号(GK)","side_stand_effective":"边撑有效","brake_driver":"刹车不断电","riding_mode":"骑行模式","signal_port_selection":"信号口选择","trigger_mode":"触发模式","conventional_brake_release_p":"刹车解P(GK专用)","one_line_coefficient":"一线通系数","hall_meter_pulse_count":"霍尔仪表脉冲数","bms_protocol":"BMS协议启用","high_speed_gear_max_speed":"高速挡最高转速","high_speed_gear_phase_line_current":"高速挡相线电流","high_speed_gear_bus_current":"高速挡母线电流","high_speed_gear_weak_magnetic_current":"高速挡弱磁电流","medium_speed_gear_max_speed":"中速挡最高转速","medium_speed_gear_phase_line_current":"中速挡相线电流","medium_speed_gear_bus_current":"中速挡母线电流","low_speed_gear_max_speed":"低速挡最高转速","low_speed_gear_phase_line_current":"低速挡相线电流","low_speed_gear_bus_current":"低速挡母线电流","shift_method":"换挡方式","momentary_default_gear":"点动默认挡位","reverse_speed":"倒挡速度","reverse_torque":"倒挡扭矩","under_voltage_point":"欠压点","over_voltage_point":"过压点","power_reduction_start_voltage":"降功率起始电压","power_reduction_end_voltage":"降功率截至电压","battery_series":"电池串数","battery_capacity":"电池容量","discharge_coefficient":"放电系数","battery_type":"电池类型","brake_feedback_torque":"刹车回馈扭矩","release_throttle_feedback_torque":"松转把回馈扭矩","regenerative_current":"反充电电流","minimum_feedback_speed":"最小回馈转速","regenerative_voltage_limit":"反充电电压上限","model_selection":"车型选择","long_press_cruise_to_reverse":"长按巡航切倒挡","warning_function":"预警功能","C_Kp":"C_Kp","C_Ki":"C_Ki","S_Kp":"S_Kp","S_Ki":"S_Ki","LF_parameter":"LF参数","SF_parameter":"SF参数","utilization":"利用率","margin":"余量","adjustment_coefficient":"调整系数","back_EMF":"反电势","rated_speed":"额定转速","carrier_frequency":"载波频率","reference_current":"学习电流","reference_1":"参数1","reference_2":"参数2","TCS_sensitivity":"TCS灵敏度"},"choice":{"forward":"正向","reverse":"反向","positiveSequence":"正序","reverseSequence":"反序","linear":"线性","softStart":"软启","motion":"运动","violent":"暴力","open":"打开","close":"关闭","force":"强制","energySaving":"节能","lowBrake":"低刹","highBrake":"高刹","invalid":"无效","toggle":"拨动","jog":"点动","highSpeedGear":"高速挡","mediumSpeedGear":"中速挡","lowSpeedGear":"低速挡","ternaryLithium":"三元锂","lithiumIronPhosphate":"磷酸铁锂","leadAcidBattery":"铅酸电池","lithiumManganate":"锰酸锂","default":"默认","cruise":"巡航","addsub":"加减挡"}},"contact":{"companyName":"苏州智骐驱动科技有限公司","address":"江苏省苏州市吴中区光福镇龙山南路9号 1号楼5楼","phone":"0512-66919004","email":"info@example.com","button1":"寄件提醒","button2":"工作时间","button3":"售后查询"},"faq_page":{"area0":"故障解析","area1":"电机/转把相关","area2":"自学习相关","area3":"性能相关","area4":"车型相关","area5":"其他问题"},"lang_page":{"tip":"您可以联系厂家定制其他国家的语言支持。"}}'
      );
    },
    /*!****************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/locales/zhft.json ***!
  \****************************************************************/
    /*! exports provided: componentName, welcomeMessage, welcome, message, home_page, ble_msg, ble_page, contact, faq_page, lang_page, default */ function (
      e
    ) {
      e.exports = JSON.parse(
        '{"componentName":"組件名稱","welcomeMessage":"歡迎使用我們的應用","welcome":"歡迎使用","message":"智科控制器","home_page":{"name":"智科控制器","grid":{"item0":"藍牙助手","item1":"新手教學","item2":"聯繫我們","item3":"常見問題","item4":"產品介紹","item5":"軟件版本","item6":"固件版本","item7":"語言切換","item8":"保留功能"}},"ble_msg":{"error":{"title":"藍牙異常","content1":"沒有藍牙權限，請檢查微信和小程序的藍牙權限","content2":"藍牙功能未打開，請打開後重試。"},"search":{"title":"搜索失敗","content":"藍牙搜索失敗，請檢查藍牙功能是否正常。","content1":"未搜索到指定藍牙設備，請確保設備已開啟並在附近。"},"conn":{"title":"連接失敗","content":"藍牙連接失敗，請檢查設備或重新嘗試。"},"discon":{"title":"連接斷開","content":"藍牙連接已斷開，請重新連接。"}},"ble_page":{"name":"藍牙助手","tabbar":{"realtime":"實時監控","parameter":"修改參數","firmwave":"固件升級","assistant":"輔助工具"},"ble":{"link":"已連接","unlink":"未連接"},"card1":{"vol":"母線電壓","cur":"母線電流","velo":"電機轉速","temp":"控制器溫度","vain":"轉把電壓","error":"故障信息","btn_learn":"自學習","btn_check":"藍牙密碼","btn_error":"故障解析","btn_switch":"切換顯示","btn_warn":"其他功能","btn_level":"重連藍牙"},"identity":{"freshman":"萌新調參 - 簡化參數","expert":"專家調參 - 全部參數","flowchart":"調參流程圖","parametertip":"參數提示開關"},"tip":"更換電機、控制器以及開啟強制鎖電機、駐坡和 TCS 功能一定要自學習！","para_title":{"cur_group":"電流組","cur_group_tip":"直接影響控制器的動力輸出","motor_group":"電機組","motor_group_tip":"電機本身以及自學習的相關參數","handle_group":"轉把和起步性能","handle_group_tip":"轉把有效區間以及動力響應相關","start_group":"功能開關","start_group_tip":"部分功能選擇是否打開","park_group":"P 擋相關","park_group_tip":"駐車擋的適配","appearance_group":"車型 / 儀表","appearance_group_tip":"儀表 / 協議 / 車型","threespeed_group":"三速","threespeed_group_tip":"不同擋位的速度和動力","rev_group":"倒車","rev_group_tip":"助力推行為倒車設置的一半","voltage_group":"電壓相關","voltage_group_tip":"適配電池電壓參數","soc_group":"SOC 相關","soc_group_tip":"估算剩餘電量","ebs_group":"EBS 相關","ebs_group_tip":"ebs 強度和有效轉速區間","advanced_group":"高級參數","advanced_group_tip":"一般不需要調整"},"operationpanel":"操作面板(讀寫參數)","actionsheet":{"readpara":"讀取參數","writepara":"寫入參數","factory":"恢復出廠","identify":"輔助調參","blekey":"修改藍牙密碼","manual":"一鍵換電","parametertip":"調參提示訊息"},"parameter":{"bus_current":"母線電流","phase_line_current":"相線電流","motor_direction":"電機方向","position_sensor_type":"位置傳感器類型","hall_sequence":"霍爾相序","encoder_sequence":"編碼器相序","encoder_accuracy":"編碼器精度","phase_shift_angle":"相移角度","pole_pairs":"極對數","full_throttle_voltage":"滿轉把電壓","idle_throttle_voltage":"空轉把電壓","forward_gear":"前進擋","reverse_gear":"倒車擋","anti_theft_lock_motor":"防盜鎖電機","lock_motor_mode":"鎖電機模式","auto_parking":"自動駐車","parking_signal":"駐車信號","side_stand_effective":"邊撑有效","brake_driver":"刹車不斷電","riding_mode":"騎行模式","signal_port_selection":"信號口選擇","trigger_mode":"觸發模式","conventional_brake_release_p":"剎車解 P (GK 專用)","one_line_coefficient":"一線通係數","hall_meter_pulse_count":"霍爾儀表脈衝數","bms_protocol":"BMS 協議啟用","high_speed_gear_max_speed":"高速擋最高轉速","high_speed_gear_phase_line_current":"高速擋相線電流","high_speed_gear_bus_current":"高速擋母線電流","high_speed_gear_weak_magnetic_current":"高速擋弱磁電流","medium_speed_gear_max_speed":"中速擋最高轉速","medium_speed_gear_phase_line_current":"中速擋相線電流","medium_speed_gear_bus_current":"中速擋母線電流","low_speed_gear_max_speed":"低速擋最高轉速","low_speed_gear_phase_line_current":"低速擋相線電流","low_speed_gear_bus_current":"低速擋母線電流","shift_method":"換擋方式","momentary_default_gear":"點動默認擋位","reverse_speed":"倒擋速度","reverse_torque":"倒擋扭矩","under_voltage_point":"欠壓點","over_voltage_point":"過壓點","power_reduction_start_voltage":"降功率起始電壓","power_reduction_end_voltage":"降功率截至電壓","battery_series":"電池串數","battery_capacity":"電池容量","discharge_coefficient":"放電係數","battery_type":"電池類型","brake_feedback_torque":"剎車回饋扭矩","release_throttle_feedback_torque":"鬆轉把回饋扭矩","regenerative_current":"反充電電流","minimum_feedback_speed":"最小回饋轉速","regenerative_voltage_limit":"反充電電壓上限","model_selection":"車型選擇","long_press_cruise_to_reverse":"長按巡航切倒擋","warning_function":"預警功能","C_Kp":"C_Kp","C_Ki":"C_Ki","S_Kp":"S_Kp","S_Ki":"S_Ki","LF_parameter":"LF 參數","SF_parameter":"SF 參數","utilization":"利用率","margin":"餘量","adjustment_coefficient":"調整係數","back_EMF":"反電勢","rated_speed":"額定轉速","carrier_frequency":"載波頻率","reference_current":"參考電流","reference_1":"參考 1","reference_2":"參考 2","TCS_sensitivity":"TCS 靈敏度"},"choice":{"forward":"正向","reverse":"反向","positiveSequence":"正序","reverseSequence":"反序","linear":"線性","softStart":"軟啟","motion":"運動","violent":"暴力","open":"打開","close":"關閉","force":"強制","energySaving":"節能","lowBrake":"低剎","highBrake":"高剎","invalid":"無效","toggle":"撥動","jog":"點動","highSpeedGear":"高速擋","mediumSpeedGear":"中速擋","lowSpeedGear":"低速擋","ternaryLithium":"三元鋰","lithiumIronPhosphate":"磷酸鐵鋰","leadAcidBattery":"鉛酸電池","lithiumManganate":"錳酸鋰","default":"默認","cruise":"巡航","addsub":"加減擋"}},"contact":{"companyName":"蘇州智騏驅動科技有限公司","address":"江蘇省蘇州市吳中區光福鎮龍山南路 9 號 1 號樓 5 樓","phone":"0512 - 66919004","email":"info@example.com","button1":"寄件提醒","button2":"工作時間","button3":"售后查询"},"faq_page":{"area0":"故障解析","area1":"電機 / 轉把相關","area2":"自學習相關","area3":"性能相關","area4":"車型相關","area5":"其他問題"},"lang_page":{"tip":"您可以聯繫廠家定制其他國家的語言支持。"}}'
      );
    },
    /*!********************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/locales/japanese.json ***!
  \********************************************************************/
    /*! exports provided: componentName, welcomeMessage, welcome, message, home_page, ble_msg, ble_page, contact, faq_page, lang_page, default */ function (
      e
    ) {
      e.exports = JSON.parse(
        '{"componentName":"コンポーネント名","welcomeMessage":"当社のアプリをご利用いただき、ありがとうございます。","welcome":"ご利用いただき、ありがとうございます。","message":"智科コントローラー","home_page":{"name":"智科コントローラー","grid":{"item0":"Bluetoothアシスタント","item1":"初心者ガイド","item2":"お問い合わせ","item3":"よくある質問","item4":"製品紹介","item5":"ソフトウェアバージョン","item6":"ファームウェアバージョン","item7":"言語切り替え","item8":"機能を維持する"}},"ble_msg":{"error":{"title":"Bluetoothエラー","content1":"Bluetoothの使用許可がないため、WeChatと小程序のBluetooth許可を確認してください。","content2":"Bluetooth機能がオフになっています。オンにしてからもう一度試してください。"},"search":{"title":"検索失敗","content":"Bluetooth検索に失敗しました。Bluetooth機能が正常に動作していることを確認してください。","content1":"指定のBluetoothデバイスが見つかりませんでした。デバイスがオンになっており、近くにあることを確認してください。"},"conn":{"title":"接続失敗","content":"Bluetooth接続に失敗しました。デバイスを確認するか、再度試してください。"},"discon":{"title":"接続切断","content":"Bluetooth接続が切断されました。再度接続してください。"}},"ble_page":{"name":"Bluetoothアシスタント","tabbar":{"realtime":"リアルタイムモニタリング","parameter":"パラメータ変更","firmwave":"ファームウェアアップデート","assistant":"パラメータ調整アシスタント"},"ble":{"link":"接続済み","unlink":"未接続"},"card1":{"vol":"バス電圧","cur":"バス電流","velo":"モーター回転数","temp":"コントローラー温度","vain":"スロットルグリップ電圧","error":"故障情報","btn_learn":"自動学習","btn_check":"Bluetoothパスワード","btn_error":"故障解析","btn_switch":"表示切り替え","btn_warn":"その他の機能","btn_level":"Bluetooth再接続"},"identity":{"freshman":"初心者パラメータ調整 - 簡易パラメータ","expert":"専門家パラメータ調整 - 全パラメータ","flowchart":"パラメータ調整フローチャート","parametertip":"パラメータヒントスイッチ"},"tip":"モーター、コントローラーを交換した場合や、強制ロックモーター、駐車ブレーキ、TCS機能を有効にする場合は必ず自動学習を行ってください！","para_title":{"cur_group":"電流グループ","cur_group_tip":"コントローラーの動力出力に直接影響します。","motor_group":"モーターグループ","motor_group_tip":"モーター自体と学習電流、弱め磁束電流に関連するパラメータです。","handle_group":"スロットルグリップと発進性能","handle_group_tip":"スロットルグリップの有効範囲と動力応答に関連します。","start_group":"機能スイッチ","start_group_tip":"よく使う機能のオン/オフを選択します。","park_group":"Pレンジ関連","park_group_tip":"駐車ブレーキの適合設定です。","appearance_group":"車種/メーター","appearance_group_tip":"メーター/プロトコル/車種に関する設定です。","threespeed_group":"3段変速","threespeed_group_tip":"異なるギアの速度と動力に関する設定です。","rev_group":"リバース","rev_group_tip":"バックアップモードの設定は、助力推進の半分に設定されます。","voltage_group":"電圧関連","voltage_group_tip":"バッテリー電圧に適合するパラメータです。","soc_group":"SOC関連","soc_group_tip":"一部の計測モジュールがない車種のSOCは、コントローラーによって推定されます。","ebs_group":"EBS関連","ebs_group_tip":"EBSの強度と有効回転数範囲に関する設定です。","advanced_group":"高度なパラメータ","advanced_group_tip":"通常は調整する必要はありません。"},"actionsheet":{"readpara":"パラメータ読み取り","writepara":"パラメータ書き込み","factory":"出荷設定に戻す","identify":"パラメータ調整支援","blekey":"Bluetoothパスワード変更","manual":"電池を交換する","parametertip":"パラメータ調整ヒント情報"},"parameter":{"bus_current":"バス電流","phase_line_current":"相線電流","motor_direction":"モーター方向","position_sensor_type":"位置センサータイプ","hall_sequence":"ホール素子の相順","encoder_sequence":"エンコーダの相順","encoder_accuracy":"エンコーダの精度","phase_shift_angle":"位相シフト角度","pole_pairs":"極対数","full_throttle_voltage":"全開スロットルグリップ電圧","idle_throttle_voltage":"アイドルスロットルグリップ電圧","forward_gear":"Dレンジのソフトスタート時間","reverse_gear":"Rレンジのソフトスタート時間","anti_theft_lock_motor":"防盗ロックモーター","lock_motor_mode":"ロックモーターモード","auto_parking":"自動駐車","parking_signal":"駐車信号","side_stand_effective":"サイドスタンド有効(ND専用)","brake_driver":"ブレーキ時の通電維持","riding_mode":"ライディングモード","signal_port_selection":"信号ポート選択","trigger_mode":"トリガーモード","conventional_brake_release_p":"ブレーキでPレンジ解除(GK専用)","one_line_coefficient":"ワンライン係数","hall_meter_pulse_count":"ホールメーターパルス数","bms_protocol":"BMSプロトコル有効化","high_speed_gear_max_speed":"高速ギアの最高回転数","high_speed_gear_phase_line_current":"高速ギアの相線電流","high_speed_gear_bus_current":"高速ギアのバス電流","high_speed_gear_weak_magnetic_current":"高速ギアの弱め磁束電流","medium_speed_gear_max_speed":"中速ギアの最高回転数","medium_speed_gear_phase_line_current":"中速ギアの相線電流","medium_speed_gear_bus_current":"中速ギアのバス電流","low_speed_gear_max_speed":"低速ギアの最高回転数","low_speed_gear_phase_line_current":"低速ギアの相線電流","low_speed_gear_bus_current":"低速ギアのバス電流","shift_method":"シフト方法","momentary_default_gear":"モーメンタリーモードのデフォルトギア","reverse_speed":"リバース速度","reverse_torque":"リバーストルク","under_voltage_point":"低電圧ポイント","over_voltage_point":"過電圧ポイント","power_reduction_start_voltage":"出力低下開始電圧","power_reduction_end_voltage":"出力低下終了電圧","battery_series":"バッテリー直列数","battery_capacity":"バッテリー容量","discharge_coefficient":"放電係数","battery_type":"バッテリータイプ","brake_feedback_torque":"ブレーキ回生トルク","release_throttle_feedback_torque":"スロットルグリップ解放時の回生トルク","regenerative_current":"回生電流","minimum_feedback_speed":"最小回生回転数","regenerative_voltage_limit":"回生電圧上限","model_selection":"車種選択","long_press_cruise_to_reverse":"長押しクルーズでリバースに切り替え","warning_function":"警報機能","C_Kp":"C_Kp","C_Ki":"C_Ki","S_Kp":"S_Kp","S_Ki":"S_Ki","LF_parameter":"LFパラメータ","SF_parameter":"SFパラメータ","utilization":"利用率","margin":"余裕","adjustment_coefficient":"調整係数","back_EMF":"逆起電力","rated_speed":"定格回転数","carrier_frequency":"キャリア周波数","reference_current":"学習電流","reference_1":"パラメータ1","reference_2":"パラメータ2","TCS_sensitivity":"TCS感度"},"choice":{"forward":"正方向","reverse":"逆方向","positiveSequence":"正相順","reverseSequence":"逆相順","linear":"線形","softStart":"ソフトスタート","motion":"モーション","violent":"パワフル","open":"オン","close":"オフ","force":"強制","energySaving":"省エネ","lowBrake":"低ブレーキ","highBrake":"高ブレーキ","invalid":"無効","toggle":"トグル","jog":"点動","highSpeedGear":"高速ギア","mediumSpeedGear":"中速ギア","lowSpeedGear":"低速ギア","ternaryLithium":"三元リチウム","lithiumIronPhosphate":"リン酸鉄リチウム","leadAcidBattery":"鉛蓄電池","lithiumManganate":"マンガン酸リチウム","default":"デフォルト","cruise":"クルーズ","addsub":"加減速ギア"}},"contact":{"companyName":"蘇州智騏駆動科技有限公司","address":"江蘇省蘇州市呉中区光福鎮龍山南路9号 1号棟5階","phone":"0512 - 66919004","email":"info@example.com","button1":"送付通知","button2":"営業時間"},"faq_page":{"area0":"故障解析","area1":"モーター/スロットルグリップ関連","area2":"自動学習関連","area3":"性能関連","area4":"車種関連","area5":"その他の質問"},"lang_page":{"tip":"その他の言語サポートについては、メーカーにご相談ください。"}}'
      );
    },
    ,
    ,
    ,
    ,
    ,
    ,
    ,
    ,
    /*!************************************************************************************************!*\
  !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/@babel/runtime/regenerator/index.js ***!
  \************************************************************************************************/
    /*! no static exports found */ function (e, t, n) {
      var r = n(/*! @babel/runtime/helpers/regeneratorRuntime */ 54)();
      e.exports = r;
    },
    /*!*******************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/regeneratorRuntime.js ***!
  \*******************************************************************/
    /*! no static exports found */ function (e, t, n) {
      var r = n(/*! ./typeof.js */ 13).default;
      function a() {
        /*! regenerator-runtime -- Copyright (c) 2014-present, Facebook, Inc. -- license (MIT): https://github.com/facebook/regenerator/blob/main/LICENSE */
        (e.exports = a =
          function () {
            return n;
          }),
          (e.exports.__esModule = !0),
          (e.exports.default = e.exports);
        var t,
          n = {},
          i = Object.prototype,
          o = i.hasOwnProperty,
          s =
            Object.defineProperty ||
            function (e, t, n) {
              e[t] = n.value;
            },
          c = "function" == typeof Symbol ? Symbol : {},
          u = c.iterator || "@@iterator",
          l = c.asyncIterator || "@@asyncIterator",
          p = c.toStringTag || "@@toStringTag";
        function d(e, t, n) {
          return (
            Object.defineProperty(e, t, {
              value: n,
              enumerable: !0,
              configurable: !0,
              writable: !0,
            }),
            e[t]
          );
        }
        try {
          d({}, "");
        } catch (t) {
          d = function (e, t, n) {
            return (e[t] = n);
          };
        }
        function f(e, t, n, r) {
          var a = t && t.prototype instanceof b ? t : b,
            i = Object.create(a.prototype),
            o = new I(r || []);
          return s(i, "_invoke", { value: E(e, n, o) }), i;
        }
        function _(e, t, n) {
          try {
            return { type: "normal", arg: e.call(t, n) };
          } catch (e) {
            return { type: "throw", arg: e };
          }
        }
        n.wrap = f;
        var h = "suspendedStart",
          m = "executing",
          v = "completed",
          g = {};
        function b() {}
        function y() {}
        function x() {}
        var w = {};
        d(w, u, function () {
          return this;
        });
        var S = Object.getPrototypeOf,
          O = S && S(S(T([])));
        O && O !== i && o.call(O, u) && (w = O);
        var A = (x.prototype = b.prototype = Object.create(w));
        function k(e) {
          ["next", "throw", "return"].forEach(function (t) {
            d(e, t, function (e) {
              return this._invoke(t, e);
            });
          });
        }
        function $(e, t) {
          function n(a, i, s, c) {
            var u = _(e[a], e, i);
            if ("throw" !== u.type) {
              var l = u.arg,
                p = l.value;
              return p && "object" == r(p) && o.call(p, "__await")
                ? t.resolve(p.__await).then(
                    function (e) {
                      n("next", e, s, c);
                    },
                    function (e) {
                      n("throw", e, s, c);
                    }
                  )
                : t.resolve(p).then(
                    function (e) {
                      (l.value = e), s(l);
                    },
                    function (e) {
                      return n("throw", e, s, c);
                    }
                  );
            }
            c(u.arg);
          }
          var a;
          s(this, "_invoke", {
            value: function (e, r) {
              function i() {
                return new t(function (t, a) {
                  n(e, r, t, a);
                });
              }
              return (a = a ? a.then(i, i) : i());
            },
          });
        }
        function E(e, n, r) {
          var a = h;
          return function (i, o) {
            if (a === m) throw Error("Generator is already running");
            if (a === v) {
              if ("throw" === i) throw o;
              return { value: t, done: !0 };
            }
            for (r.method = i, r.arg = o; ; ) {
              var s = r.delegate;
              if (s) {
                var c = j(s, r);
                if (c) {
                  if (c === g) continue;
                  return c;
                }
              }
              if ("next" === r.method) r.sent = r._sent = r.arg;
              else if ("throw" === r.method) {
                if (a === h) throw ((a = v), r.arg);
                r.dispatchException(r.arg);
              } else "return" === r.method && r.abrupt("return", r.arg);
              a = m;
              var u = _(e, n, r);
              if ("normal" === u.type) {
                if (((a = r.done ? v : "suspendedYield"), u.arg === g))
                  continue;
                return { value: u.arg, done: r.done };
              }
              "throw" === u.type &&
                ((a = v), (r.method = "throw"), (r.arg = u.arg));
            }
          };
        }
        function j(e, n) {
          var r = n.method,
            a = e.iterator[r];
          if (a === t)
            return (
              (n.delegate = null),
              ("throw" === r &&
                e.iterator.return &&
                ((n.method = "return"),
                (n.arg = t),
                j(e, n),
                "throw" === n.method)) ||
                ("return" !== r &&
                  ((n.method = "throw"),
                  (n.arg = new TypeError(
                    "The iterator does not provide a '" + r + "' method"
                  )))),
              g
            );
          var i = _(a, e.iterator, n.arg);
          if ("throw" === i.type)
            return (
              (n.method = "throw"), (n.arg = i.arg), (n.delegate = null), g
            );
          var o = i.arg;
          return o
            ? o.done
              ? ((n[e.resultName] = o.value),
                (n.next = e.nextLoc),
                "return" !== n.method && ((n.method = "next"), (n.arg = t)),
                (n.delegate = null),
                g)
              : o
            : ((n.method = "throw"),
              (n.arg = new TypeError("iterator result is not an object")),
              (n.delegate = null),
              g);
        }
        function P(e) {
          var t = { tryLoc: e[0] };
          1 in e && (t.catchLoc = e[1]),
            2 in e && ((t.finallyLoc = e[2]), (t.afterLoc = e[3])),
            this.tryEntries.push(t);
        }
        function C(e) {
          var t = e.completion || {};
          (t.type = "normal"), delete t.arg, (e.completion = t);
        }
        function I(e) {
          (this.tryEntries = [{ tryLoc: "root" }]),
            e.forEach(P, this),
            this.reset(!0);
        }
        function T(e) {
          if (e || "" === e) {
            var n = e[u];
            if (n) return n.call(e);
            if ("function" == typeof e.next) return e;
            if (!isNaN(e.length)) {
              var a = -1,
                i = function n() {
                  for (; ++a < e.length; )
                    if (o.call(e, a)) return (n.value = e[a]), (n.done = !1), n;
                  return (n.value = t), (n.done = !0), n;
                };
              return (i.next = i);
            }
          }
          throw new TypeError(r(e) + " is not iterable");
        }
        return (
          (y.prototype = x),
          s(A, "constructor", { value: x, configurable: !0 }),
          s(x, "constructor", { value: y, configurable: !0 }),
          (y.displayName = d(x, p, "GeneratorFunction")),
          (n.isGeneratorFunction = function (e) {
            var t = "function" == typeof e && e.constructor;
            return (
              !!t &&
              (t === y || "GeneratorFunction" === (t.displayName || t.name))
            );
          }),
          (n.mark = function (e) {
            return (
              Object.setPrototypeOf
                ? Object.setPrototypeOf(e, x)
                : ((e.__proto__ = x), d(e, p, "GeneratorFunction")),
              (e.prototype = Object.create(A)),
              e
            );
          }),
          (n.awrap = function (e) {
            return { __await: e };
          }),
          k($.prototype),
          d($.prototype, l, function () {
            return this;
          }),
          (n.AsyncIterator = $),
          (n.async = function (e, t, r, a, i) {
            void 0 === i && (i = Promise);
            var o = new $(f(e, t, r, a), i);
            return n.isGeneratorFunction(t)
              ? o
              : o.next().then(function (e) {
                  return e.done ? e.value : o.next();
                });
          }),
          k(A),
          d(A, p, "Generator"),
          d(A, u, function () {
            return this;
          }),
          d(A, "toString", function () {
            return "[object Generator]";
          }),
          (n.keys = function (e) {
            var t = Object(e),
              n = [];
            for (var r in t) n.push(r);
            return (
              n.reverse(),
              function e() {
                for (; n.length; ) {
                  var r = n.pop();
                  if (r in t) return (e.value = r), (e.done = !1), e;
                }
                return (e.done = !0), e;
              }
            );
          }),
          (n.values = T),
          (I.prototype = {
            constructor: I,
            reset: function (e) {
              if (
                ((this.prev = 0),
                (this.next = 0),
                (this.sent = this._sent = t),
                (this.done = !1),
                (this.delegate = null),
                (this.method = "next"),
                (this.arg = t),
                this.tryEntries.forEach(C),
                !e)
              )
                for (var n in this)
                  "t" === n.charAt(0) &&
                    o.call(this, n) &&
                    !isNaN(+n.slice(1)) &&
                    (this[n] = t);
            },
            stop: function () {
              this.done = !0;
              var e = this.tryEntries[0].completion;
              if ("throw" === e.type) throw e.arg;
              return this.rval;
            },
            dispatchException: function (e) {
              if (this.done) throw e;
              var n = this;
              function r(r, a) {
                return (
                  (s.type = "throw"),
                  (s.arg = e),
                  (n.next = r),
                  a && ((n.method = "next"), (n.arg = t)),
                  !!a
                );
              }
              for (var a = this.tryEntries.length - 1; a >= 0; --a) {
                var i = this.tryEntries[a],
                  s = i.completion;
                if ("root" === i.tryLoc) return r("end");
                if (i.tryLoc <= this.prev) {
                  var c = o.call(i, "catchLoc"),
                    u = o.call(i, "finallyLoc");
                  if (c && u) {
                    if (this.prev < i.catchLoc) return r(i.catchLoc, !0);
                    if (this.prev < i.finallyLoc) return r(i.finallyLoc);
                  } else if (c) {
                    if (this.prev < i.catchLoc) return r(i.catchLoc, !0);
                  } else {
                    if (!u)
                      throw Error("try statement without catch or finally");
                    if (this.prev < i.finallyLoc) return r(i.finallyLoc);
                  }
                }
              }
            },
            abrupt: function (e, t) {
              for (var n = this.tryEntries.length - 1; n >= 0; --n) {
                var r = this.tryEntries[n];
                if (
                  r.tryLoc <= this.prev &&
                  o.call(r, "finallyLoc") &&
                  this.prev < r.finallyLoc
                ) {
                  var a = r;
                  break;
                }
              }
              a &&
                ("break" === e || "continue" === e) &&
                a.tryLoc <= t &&
                t <= a.finallyLoc &&
                (a = null);
              var i = a ? a.completion : {};
              return (
                (i.type = e),
                (i.arg = t),
                a
                  ? ((this.method = "next"), (this.next = a.finallyLoc), g)
                  : this.complete(i)
              );
            },
            complete: function (e, t) {
              if ("throw" === e.type) throw e.arg;
              return (
                "break" === e.type || "continue" === e.type
                  ? (this.next = e.arg)
                  : "return" === e.type
                  ? ((this.rval = this.arg = e.arg),
                    (this.method = "return"),
                    (this.next = "end"))
                  : "normal" === e.type && t && (this.next = t),
                g
              );
            },
            finish: function (e) {
              for (var t = this.tryEntries.length - 1; t >= 0; --t) {
                var n = this.tryEntries[t];
                if (n.finallyLoc === e)
                  return this.complete(n.completion, n.afterLoc), C(n), g;
              }
            },
            catch: function (e) {
              for (var t = this.tryEntries.length - 1; t >= 0; --t) {
                var n = this.tryEntries[t];
                if (n.tryLoc === e) {
                  var r = n.completion;
                  if ("throw" === r.type) {
                    var a = r.arg;
                    C(n);
                  }
                  return a;
                }
              }
              throw Error("illegal catch attempt");
            },
            delegateYield: function (e, n, r) {
              return (
                (this.delegate = { iterator: T(e), resultName: n, nextLoc: r }),
                "next" === this.method && (this.arg = t),
                g
              );
            },
          }),
          n
        );
      }
      (e.exports = a),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!*****************************************************************!*\
  !*** ./node_modules/@babel/runtime/helpers/asyncToGenerator.js ***!
  \*****************************************************************/
    /*! no static exports found */ function (e, t) {
      function n(e, t, n, r, a, i, o) {
        try {
          var s = e[i](o),
            c = s.value;
        } catch (e) {
          return void n(e);
        }
        s.done ? t(c) : Promise.resolve(c).then(r, a);
      }
      (e.exports = function (e) {
        return function () {
          var t = this,
            r = arguments;
          return new Promise(function (a, i) {
            var o = e.apply(t, r);
            function s(e) {
              n(o, a, i, s, c, "next", e);
            }
            function c(e) {
              n(o, a, i, s, c, "throw", e);
            }
            s(void 0);
          });
        };
      }),
        (e.exports.__esModule = !0),
        (e.exports.default = e.exports);
    },
    /*!*******************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/utils/crypto-util.js ***!
  \*******************************************************************/
    /*! no static exports found */ function (e, t, n) {
      (function (e) {
        var r = n(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
        Object.defineProperty(t, "__esModule", { value: !0 }),
          (t.decryptData = function (e, t) {
            return u.apply(this, arguments);
          }),
          (t.encryptData = function (e, t, n, r) {
            var a = t.replace(/[-: ]/g, ""),
              i = n.toString().padStart(4, "0"),
              o = r.toString().padStart(4, "0"),
              c = "".concat(a, "-").concat(i, "-").concat(o);
            console.log("plaintext" + c);
            for (var u = "", l = 0; l < c.length; l++) {
              var p = l % e.length,
                d = e.charCodeAt(p),
                f = c.charCodeAt(l);
              u += String.fromCharCode(f + d);
            }
            return (function (e) {
              var t = [],
                n = 0;
              for (; n < e.length; ) {
                var r = e.codePointAt(n);
                r < 128
                  ? t.push(r)
                  : r < 2048
                  ? (t.push(192 | (r >> 6)), t.push(128 | (63 & r)))
                  : r < 65536
                  ? (t.push(224 | (r >> 12)),
                    t.push(128 | ((r >> 6) & 63)),
                    t.push(128 | (63 & r)))
                  : (t.push(240 | (r >> 18)),
                    t.push(128 | ((r >> 12) & 63)),
                    t.push(128 | ((r >> 6) & 63)),
                    t.push(128 | (63 & r)),
                    n++),
                  n++;
              }
              for (var a = "", i = 0; i < t.length; i += 3) {
                var o = t[i],
                  c = t[i + 1],
                  u = t[i + 2],
                  l = ((15 & c) << 2) | (u >> 6 || 0),
                  p = 63 & u,
                  d = s[o >> 2] + s[((3 & o) << 4) | (c >> 4 || 0)];
                a += d +=
                  void 0 === c ? "==" : void 0 === u ? s[l] + "=" : s[l] + s[p];
              }
              return a;
            })(u);
          });
        var a = r(n(/*! @babel/runtime/regenerator */ 53)),
          i = r(n(/*! @babel/runtime/helpers/slicedToArray */ 5)),
          o = r(n(/*! @babel/runtime/helpers/asyncToGenerator */ 55)),
          s =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        function c(e) {
          e = e.replace(/=+$/, "");
          for (var t = "", n = 0; n < e.length; n += 4) {
            var r = s.indexOf(e[n]),
              a = s.indexOf(e[n + 1]),
              i = e[n + 2] ? s.indexOf(e[n + 2]) : -1,
              o = e[n + 3] ? s.indexOf(e[n + 3]) : -1,
              c = (r << 2) | (a >> 4);
            if (((t += String.fromCharCode(c)), -1 !== i)) {
              var u = ((15 & a) << 4) | (i >> 2);
              if (((t += String.fromCharCode(u)), -1 !== o)) {
                var l = ((3 & i) << 6) | o;
                t += String.fromCharCode(l);
              }
            }
          }
          return decodeURIComponent(escape(t));
        }
        function u() {
          return (u = (0, o.default)(
            a.default.mark(function e(t, n) {
              var r, i, o, s, u, p, d, f, _, h, m;
              return a.default.wrap(
                function (e) {
                  for (;;)
                    switch ((e.prev = e.next)) {
                      case 0:
                        for (
                          e.prev = 0, r = c(n), i = "", o = 0;
                          o < r.length;
                          o++
                        )
                          (s = o % t.length),
                            (u = t.charCodeAt(s)),
                            (p = r.charCodeAt(o)),
                            (i += String.fromCharCode(p - u));
                        return (
                          console.log("decryptedText" + i),
                          (d = i.slice(0, 12)),
                          (f = i.slice(13, 17)),
                          (_ = i.slice(18)),
                          (h = new Date(
                            parseInt(d.slice(0, 4)),
                            parseInt(d.slice(4, 6)) - 1,
                            parseInt(d.slice(6, 8)),
                            parseInt(d.slice(8, 10)),
                            parseInt(d.slice(10, 12))
                          )),
                          (e.next = 11),
                          l()
                        );
                      case 11:
                        if (((m = e.sent), !(Math.abs((m - h) / 6e4) < 3))) {
                          e.next = 17;
                          break;
                        }
                        return e.abrupt("return", {
                          time: h,
                          num1: parseInt(f),
                          num2: parseInt(_),
                        });
                      case 17:
                        throw new Error("指令失效");
                      case 18:
                        e.next = 24;
                        break;
                      case 20:
                        return (
                          (e.prev = 20),
                          (e.t0 = e.catch(0)),
                          console.error("解密出错:", e.t0),
                          e.abrupt("return", null)
                        );
                      case 24:
                      case "end":
                        return e.stop();
                    }
                },
                e,
                null,
                [[0, 20]]
              );
            })
          )).apply(this, arguments);
        }
        function l() {
          return p.apply(this, arguments);
        }
        function p() {
          return (p = (0, o.default)(
            a.default.mark(function t() {
              return a.default.wrap(function (t) {
                for (;;)
                  switch ((t.prev = t.next)) {
                    case 0:
                      return (
                        e.request({
                          url: "https://quan.suning.com/getSysTime.do",
                          method: "GET",
                          timeout: 1e4,
                          success: function (e) {
                            if (200 === e.statusCode) {
                              if (e.sysTime2) {
                                var t = networkTimeObj.sysTime2.split(" "),
                                  n = (0, i.default)(t, 2),
                                  r = n[0],
                                  a = n[1],
                                  o = r.split("-"),
                                  s = (0, i.default)(o, 3),
                                  c = s[0],
                                  u = s[1],
                                  l = s[2],
                                  p = a.split(":"),
                                  d = (0, i.default)(p, 3),
                                  f = d[0],
                                  _ = d[1],
                                  h = d[2];
                                return new Date(c, u - 1, l, f, _, h);
                              }
                            } else
                              console.error("请求失败，状态码:", e.statusCode);
                          },
                          fail: function (e) {
                            console.error("请求出错:", e);
                          },
                        }),
                        t.abrupt("return", new Date())
                      );
                    case 2:
                    case "end":
                      return t.stop();
                  }
              }, t);
            })
          )).apply(this, arguments);
        }
      }).call(
        this,
        n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2).default
      );
    },
    ,
    ,
    ,
    ,
    ,
    ,
    ,
    ,
    /*!**********************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/utils/parameter_list.js ***!
  \**********************************************************************/
    /*! no static exports found */ function (e, t, n) {
      (function (e) {
        var r = n(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
        Object.defineProperty(t, "__esModule", { value: !0 }),
          (t.renderDataToOriginal =
            t.parameter_process_data =
            t.loadAndUpdateData =
            t.getparameter_title =
            t.getparameter_sheet =
            t.getparameter_setting =
            t.getIdentity =
            t.exportAndChooseAction =
            t.buttonDataMap =
              void 0);
        var a = r(n(/*! @babel/runtime/helpers/toConsumableArray */ 18)),
          i = r(n(/*! @babel/runtime/helpers/defineProperty */ 11)),
          o = n(/*! @/utils/i18n.js */ 40),
          s = r(n(/*! @/utils/hardwareConfig.js */ 66)),
          c = n(/*! @/utils/parametertable.js */ 67);
        function u(e, t) {
          var n = Object.keys(e);
          if (Object.getOwnPropertySymbols) {
            var r = Object.getOwnPropertySymbols(e);
            t &&
              (r = r.filter(function (t) {
                return Object.getOwnPropertyDescriptor(e, t).enumerable;
              })),
              n.push.apply(n, r);
          }
          return n;
        }
        function l(e) {
          for (var t = 1; t < arguments.length; t++) {
            var n = null != arguments[t] ? arguments[t] : {};
            t % 2
              ? u(Object(n), !0).forEach(function (t) {
                  (0, i.default)(e, t, n[t]);
                })
              : Object.getOwnPropertyDescriptors
              ? Object.defineProperties(e, Object.getOwnPropertyDescriptors(n))
              : u(Object(n)).forEach(function (t) {
                  Object.defineProperty(
                    e,
                    t,
                    Object.getOwnPropertyDescriptor(n, t)
                  );
                });
          }
          return e;
        }
        function p(e, t) {
          var n =
            ("undefined" != typeof Symbol && e[Symbol.iterator]) ||
            e["@@iterator"];
          if (!n) {
            if (
              Array.isArray(e) ||
              (n = (function (e, t) {
                if (!e) return;
                if ("string" == typeof e) return d(e, t);
                var n = Object.prototype.toString.call(e).slice(8, -1);
                "Object" === n && e.constructor && (n = e.constructor.name);
                if ("Map" === n || "Set" === n) return Array.from(e);
                if (
                  "Arguments" === n ||
                  /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)
                )
                  return d(e, t);
              })(e)) ||
              (t && e && "number" == typeof e.length)
            ) {
              n && (e = n);
              var r = 0,
                a = function () {};
              return {
                s: a,
                n: function () {
                  return r >= e.length
                    ? { done: !0 }
                    : { done: !1, value: e[r++] };
                },
                e: function (e) {
                  throw e;
                },
                f: a,
              };
            }
            throw new TypeError(
              "Invalid attempt to iterate non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."
            );
          }
          var i,
            o = !0,
            s = !1;
          return {
            s: function () {
              n = n.call(e);
            },
            n: function () {
              var e = n.next();
              return (o = e.done), e;
            },
            e: function (e) {
              (s = !0), (i = e);
            },
            f: function () {
              try {
                o || null == n.return || n.return();
              } finally {
                if (s) throw i;
              }
            },
          };
        }
        function d(e, t) {
          (null == t || t > e.length) && (t = e.length);
          for (var n = 0, r = new Array(t); n < t; n++) r[n] = e[n];
          return r;
        }
        t.buttonDataMap = {
          button_1_1: {
            headers: ["车型", "极对数", "霍尔角度"],
            data: [
              ["九号E80", "23", "120"],
              ["九号E100", "30", "120"],
              ["九号E125", "30", "120"],
              ["九号N70C", "23", "120"],
              ["九号N90C", "23", "120"],
              ["九号F90", "27", "120"],
              ["九号Max90", "27", "120"],
              ["小牛N金宇星", "27", "120"],
              ["小牛N博世", "30", "120"],
              ["小牛U博世", "30", "120"],
              ["小牛M金宇星", "24", "120"],
            ],
          },
          button_1_2: {
            headers: ["电机", "极对数", "相移角度"],
            data: [
              ["全顺10寸磁钢", "23", "-60"],
              ["全顺12寸磁钢", "28", "-60"],
              ["全顺10寸瓦片", "16", "-120"],
              ["全顺12寸瓦片", "16", "-120"],
              ["全顺5代寸瓦片", "16", "-120"],
              ["御马10寸磁钢", "24", "-60"],
              ["御马12寸磁钢", "28", "-60"],
              ["御马10寸瓦片", "16", "-60"],
              ["御马15寸瓦片", "24", "-60"],
              ["熊猫10寸瓦片", "16", "-120"],
              ["熊猫10寸磁钢", "23", "-60"],
              ["熊猫12寸瓦片", "24", "-120"],
            ],
          },
          ae_car_sel: {
            headers: ["车型", "带线束控", "直上控"],
            data: [
              ["AE5", "AE5", "AE5"],
              ["老款AE2", "AE4/AE2(带线束)", "AE4/AE2(直上控)"],
              ["新款AE2", "AE4/AE2(带线束)", "AE4/AE2(直上控)"],
              ["AE4电摩", "AE4/AE2(带线束)", "AE4/AE2(带线束)"],
              ["AE4电自", "AE4/AE2(带线束)", "AE4/AE2(直上控)"],
              ["25款AE4", "25款AE4电摩", "25款AE4电摩"],
            ],
          },
        };
        t.getIdentity = function () {
          return [
            (0, o.t)("ble_page.identity.freshman"),
            (0, o.t)("ble_page.identity.expert"),
            (0, o.t)("ble_page.identity.flowchart"),
            (0, o.t)("ble_page.identity.parametertip"),
          ];
        };
        t.getparameter_title = function () {
          return [
            {
              id: 0,
              icon: "iconfont icon-UIicon_dianliu text-white",
              name: (0, o.t)("ble_page.para_title.cur_group"),
              count: 2,
              about: (0, o.t)("ble_page.para_title.cur_group_tip"),
              isarrow: !1,
              visible: !0,
              buttons: [
                { id: "button_0_1", name: "一键换电设置" },
                { id: "button_0_2", name: "满把断电排查" },
              ],
            },
            {
              id: 1,
              icon: "iconfont icon-jiansudianji text-white",
              name: (0, o.t)("ble_page.para_title.motor_group"),
              count: 5,
              about: (0, o.t)("ble_page.para_title.motor_group_tip"),
              isarrow: !1,
              visible: !0,
              buttons: [
                { id: "button_1_2", name: "常见电机参数" },
                { id: "button_1_3", name: "弱磁介绍" },
                { id: "button_1_4", name: "通用弱磁" },
                { id: "button_1_5", name: "学习电流" },
              ],
            },
            {
              id: 2,
              icon: "iconfont icon-taban text-white",
              name: (0, o.t)("ble_page.para_title.handle_group"),
              count: 5,
              about: (0, o.t)("ble_page.para_title.handle_group_tip"),
              isarrow: !1,
              visible: !0,
              buttons: [
                { id: "button_2_1", name: "强劲起步" },
                { id: "button_2_2", name: "日常起步" },
                { id: "button_2_3", name: "适配教程" },
              ],
            },
            {
              id: 3,
              icon: "iconfont icon-jiasu text-white ",
              name: (0, o.t)("ble_page.para_title.start_group"),
              count: 5,
              about: (0, o.t)("ble_page.para_title.start_group_tip"),
              isarrow: !1,
              visible: !0,
              buttons: [
                { id: "button_3_1", name: "Tcs说明" },
                { id: "button_3_2", name: "倒挡说明" },
                { id: "button_3_3", name: "预警说明" },
              ],
            },
            {
              id: 4,
              icon: "iconfont icon-icon_zidongzhuche text-white ",
              name: (0, o.t)("ble_page.para_title.park_group"),
              count: 5,
              about: (0, o.t)("ble_page.para_title.park_group_tip"),
              isarrow: !1,
              visible: !0,
              buttons: [],
            },
            {
              id: 5,
              icon: "iconfont icon-yibiaoban text-white ",
              name: (0, o.t)("ble_page.para_title.appearance_group"),
              count: 5,
              about: (0, o.t)("ble_page.para_title.appearance_group_tip"),
              isarrow: !1,
              visible: !0,
              buttons: [
                { id: "button_5_1", name: "表显车速不准" },
                { id: "button_5_2", name: "骑行里程不准" },
                { id: "button_5_3", name: "和车型相关" },
              ],
            },
            {
              id: 6,
              icon: "iconfont icon-zhuansu text-white ",
              name: (0, o.t)("ble_page.para_title.threespeed_group"),
              count: 5,
              about: (0, o.t)("ble_page.para_title.threespeed_group_tip"),
              isarrow: !1,
              visible: !0,
              buttons: [
                { id: "button_6_1", name: "换挡接线" },
                { id: "button_6_2", name: "无换挡器" },
              ],
            },
            {
              id: 7,
              icon: "iconfont icon-daochedeng text-white ",
              name: (0, o.t)("ble_page.para_title.rev_group"),
              count: 5,
              about: (0, o.t)("ble_page.para_title.rev_group_tip"),
              isarrow: !1,
              visible: !0,
              buttons: [],
            },
            {
              id: 8,
              icon: "iconfont icon-xudianchidianya text-white ",
              name: (0, o.t)("ble_page.para_title.voltage_group"),
              count: 5,
              about: (0, o.t)("ble_page.para_title.voltage_group_tip"),
              isarrow: !1,
              visible: !0,
              buttons: [
                { id: "button_8_1", name: "48V通用" },
                { id: "button_8_2", name: "60V通用" },
                { id: "button_8_3", name: "64V通用" },
                { id: "button_8_4", name: "72V通用" },
                { id: "button_8_5", name: "76V通用" },
              ],
            },
            {
              id: 9,
              icon: "iconfont icon-xudianchidianya text-white ",
              name: (0, o.t)("ble_page.para_title.soc_group"),
              count: 5,
              about: (0, o.t)("ble_page.para_title.soc_group_tip"),
              isarrow: !1,
              visible: !0,
              buttons: [{ id: "button_9_1", name: "参数说明" }],
            },
            {
              id: 10,
              icon: "iconfont icon-jiasu text-white ",
              name: (0, o.t)("ble_page.para_title.ebs_group"),
              count: 5,
              about: (0, o.t)("ble_page.para_title.ebs_group_tip"),
              isarrow: !1,
              visible: !0,
              buttons: [
                { id: "button_10_1", name: "通用设置" },
                { id: "button_10_2", name: "中度设置" },
                { id: "button_10_3", name: "关闭EBS" },
              ],
            },
            {
              id: 11,
              icon: "iconfont icon-jiasu text-white ",
              name: (0, o.t)("ble_page.para_title.advanced_group"),
              count: 5,
              about: (0, o.t)("ble_page.para_title.advanced_group_tip"),
              isarrow: !1,
              visible: !0,
              buttons: [{ id: "button_11_1", name: "S_Ki" }],
            },
          ];
        };
        var f = {
          4: {
            options: ["普通", "110P", "200P", "MZ"],
            mapping: { 0: 0, 1: 1, 2: 2, 3: 3 },
          },
          8: {
            options: ["485车型", "普通", "一线通", "AE", "NXT", "FX"],
            description: [
              "485中控",
              "降级普通电摩",
              "一线通",
              "AE",
              "NXT",
              "FX",
            ],
            mapping: { 0: 0, 1: 1, 2: 2, 3: 3, 4: 4, 5: 5 },
          },
          3: {
            options: ["AE5", "AE4/AE2(带线)", "25款AE4电摩", "AE4/AE2(直上)"],
            description: [
              "AE5",
              "AE4/AE2(带线)",
              "25款AE4电摩",
              "AE4/AE2(直上)",
            ],
            mapping: { 0: 0, 1: 1, 2: 2, 3: 3 },
          },
          7: {
            options: ["MZ", "NX Sport", "SQ"],
            mapping: { 0: 0, 11: 1, 7: 2 },
          },
          5: {
            options: ["普通", "祖玛", "Ube", "Ube2024"],
            mapping: { 0: 0, 1: 1, 2: 2, 3: 3 },
          },
          0: { options: [], mapping: {} },
          6: { options: ["PD485", "GD485"], mapping: { 0: 0, 1: 1 } },
        };
        t.parameter_process_data = function (e, t) {
          var n = (0, c.getParameterSetting)(t),
            r = [],
            a = e[61],
            i = 15 & a,
            u = 240 & a;
          u >>= 4;
          var d = 3840 & a;
          d >>= 8;
          var _ = 61440 & a;
          (_ >>= 12),
            t >= 27 ||
              (8 != _ && 3 != _ && 6 != _) ||
              (n[5][3].name = (0, o.t)("ble_page.parameter.bms_protocol"));
          var h = s.default[d].subSeries[u].subSeries[i].ibus,
            m = s.default[d].subSeries[u].subSeries[i].iphase;
          (n[0][0].max = h),
            (n[0][1].max = m),
            f[_].options &&
              ((n[5][0].options = f[_].options),
              (n[5][0].mapping = f[_].mapping),
              (n[5][0].description = f[_].description));
          var v,
            g = p(n);
          try {
            for (g.s(); !(v = g.n()).done; ) {
              var b,
                y = v.value,
                x = [],
                w = p(y);
              try {
                for (w.s(); !(b = w.n()).done; ) {
                  var S = b.value,
                    O = S.type,
                    A = S.index,
                    k = S.bit_start,
                    $ = S.bit_end,
                    E = S.scale,
                    j = S.bit0,
                    P = S.bit1,
                    C = S.bit2,
                    I = S.mapping,
                    T = S.continuebit,
                    L = e[A];
                  if (void 0 !== k && void 0 !== $)
                    L = (L & (((1 << ($ - k + 1)) - 1) << k)) >> k;
                  var D = void 0;
                  switch (O) {
                    case "uint":
                      D = Math.round(L / E);
                      break;
                    case "int":
                      var M = $ - k + 1;
                      L & (1 << (M - 1)) && (L -= 1 << M),
                        (D = Math.round(L / E));
                      break;
                    case "float":
                      D = parseFloat((L / E).toFixed(2));
                      break;
                    case "bool":
                      D = 0 != (1 & L);
                      break;
                    case "list":
                      var B = 0;
                      if (1 == T) B = L;
                      else
                        for (
                          var F = [j, P, C], N = 0;
                          N < 3 && -1 !== F[N];
                          N++
                        ) {
                          B |= ((L >> F[N]) & 1) << N;
                        }
                      D = I && void 0 !== I[B] ? I[B] : B;
                      break;
                    default:
                      D = L;
                  }
                  D = Math.max(S.min, Math.min(S.max, D));
                  var V = l(l({}, S), {}, { value: D });
                  x.push(V);
                }
              } catch (e) {
                w.e(e);
              } finally {
                w.f();
              }
              r.push(x);
            }
          } catch (e) {
            g.e(e);
          } finally {
            g.f();
          }
          return r;
        };
        t.renderDataToOriginal = function (e, t) {
          if (!Array.isArray(t))
            return console.error("renderData must be an array"), [];
          if (0 === t.length) return console.error("renderData is empty"), [];
          var n,
            r = t.flat(),
            i = (0, a.default)(e),
            o = p(r);
          try {
            for (o.s(); !(n = o.n()).done; ) {
              var s = n.value,
                c = s.type,
                u = s.index,
                l = s.bit_start,
                d = s.bit_end,
                f = s.scale,
                _ = s.bit0,
                h = s.bit1,
                m = s.bit2,
                v = s.mapping,
                g = s.continuebit,
                b = (s.name, s.value),
                y = 0;
              switch (c) {
                case "uint":
                  y = Math.round(b * f);
                  break;
                case "int":
                  if ((y = Math.round(b * f)) < 0) y += 1 << (d - l + 1);
                  break;
                case "float":
                  y = Math.round(b * f);
                  break;
                case "bool":
                  y = b ? 1 : 0;
                  break;
                case "list":
                  var x = {};
                  if (v) for (var w in v) x[v[w]] = parseInt(w);
                  if (((y = void 0 !== x[b] ? x[b] : b), 1 == g)) {
                    var S = ~(((1 << (d - l + 1)) - 1) << l);
                    (i[u] &= S), (i[u] |= y << l);
                  } else {
                    for (
                      var O = 0, A = 0, k = [_, h, m], $ = 0;
                      $ < 3 && -1 !== k[$];
                      $++
                    )
                      (A |= 1 << k[$]), 0 != (y & (1 << $)) && (O |= 1 << k[$]);
                    (i[u] &= ~A), (i[u] |= O);
                  }
                  break;
                default:
                  y = b;
              }
              if ("list" !== c) {
                var E = ~(((1 << (d - l + 1)) - 1) << l);
                (i[u] &= E), (i[u] |= y << l);
              }
            }
          } catch (e) {
            o.e(e);
          } finally {
            o.f();
          }
          console.log("resultData:", i);
          var j = 0,
            P = [];
          (P[0] = 170), (P[1] = 18), (P[2] = 0);
          for (var C = 0; C < 63; C++)
            C < i.length &&
              ((j += i[C]),
              (j &= 65535),
              (P[2 * C + 3] = (i[C] >> 0) & 255),
              (P[2 * C + 4] = (i[C] >> 8) & 255));
          return (
            (i[63] = 65535 - j),
            (P[129] = (i[63] >> 0) & 255),
            (P[130] = (i[63] >> 8) & 255),
            P
          );
        };
        t.getparameter_sheet = function () {
          return [
            (0, o.t)("ble_page.actionsheet.readpara"),
            (0, o.t)("ble_page.actionsheet.writepara"),
            (0, o.t)("ble_page.actionsheet.identify"),
            (0, o.t)("ble_page.actionsheet.factory"),
            (0, o.t)("ble_page.actionsheet.blekey"),
            (0, o.t)("ble_page.actionsheet.manual"),
          ];
        };
        t.exportAndChooseAction = function (t, n) {
          var r = [];
          t.forEach(function (e) {
            e.forEach(function (e) {
              var t = { name: e.name, value: e.value };
              "bool" === e.type
                ? (t.text = e.value ? e.onText : e.offText)
                : "list" === e.type && (t.text = e.options[e.value]),
                r.push(t);
            });
          });
          var a = n
            .map(function (e) {
              return "0x" + e.toString(16).padStart(4, "0").toUpperCase();
            })
            .join(", ");
          r.push({ name: "rawData", value: a });
          var i = JSON.stringify(r, null, 2),
            o = new Date(),
            s = [
              o.getFullYear(),
              String(o.getMonth() + 1).padStart(2, "0"),
              String(o.getDate()).padStart(2, "0"),
              "_",
              String(o.getHours()).padStart(2, "0"),
              String(o.getMinutes()).padStart(2, "0"),
              String(o.getSeconds()).padStart(2, "0"),
            ].join(""),
            c = e.getFileSystemManager(),
            u = ""
              .concat(e.env.USER_DATA_PATH, "/parameters_")
              .concat(s, ".json");
          c.writeFile({
            filePath: u,
            data: i,
            encoding: "utf8",
            success: function () {
              e.showActionSheet({
                itemList: ["分享", "收藏"],
                success: function (t) {
                  var n = t.tapIndex;
                  0 === n
                    ? e.shareFileMessage({
                        filePath: u,
                        success: function () {
                          e.showToast({ title: "分享成功", icon: "success" });
                        },
                        fail: function (t) {
                          e.showToast({ title: "分享失败", icon: "none" }),
                            console.error("分享失败:", t);
                        },
                      })
                    : 1 === n &&
                      e.setStorage({
                        key: "collectedFile",
                        data: u,
                        success: function () {
                          e.showToast({ title: "收藏成功", icon: "success" });
                        },
                        fail: function (t) {
                          e.showToast({ title: "收藏失败", icon: "none" }),
                            console.error("收藏失败:", t);
                        },
                      });
                },
                fail: function (e) {
                  console.error("操作菜单显示失败:", e);
                },
              });
            },
            fail: function (t) {
              e.showToast({ title: "文件保存失败", icon: "none" }),
                console.error("文件保存失败:", t);
            },
          });
        };
        t.loadAndUpdateData = function (t) {
          e.showActionSheet({
            itemList: ["从分享文件加载", "从收藏文件加载"],
            success: function (n) {
              var r = n.tapIndex,
                a = "";
              0 === r
                ? e.chooseMessageFile({
                    count: 1,
                    type: "file",
                    success: function (e) {
                      (a = e.tempFiles[0].path), _(a, t);
                    },
                    fail: function (t) {
                      e.showToast({ title: "选择分享文件失败", icon: "none" }),
                        console.error("选择分享文件失败:", t);
                    },
                  })
                : 1 === r &&
                  e.getStorage({
                    key: "collectedFile",
                    success: function (e) {
                      (a = e.data), _(a, t);
                    },
                    fail: function (t) {
                      e.showToast({ title: "获取收藏文件失败", icon: "none" }),
                        console.error("获取收藏文件失败:", t);
                    },
                  });
            },
            fail: function (e) {
              console.error("操作菜单显示失败:", e);
            },
          });
        };
        var _ = function (t, n) {
          e.getFileSystemManager().readFile({
            filePath: t,
            encoding: "utf8",
            success: function (t) {
              try {
                var r = JSON.parse(t.data);
                n.forEach(function (e) {
                  e.forEach(function (e) {
                    var t = r.find(function (t) {
                      return t.name === e.name;
                    });
                    t && (e.value = t.value);
                  });
                }),
                  e.showToast({ title: "数据更新成功", icon: "success" });
              } catch (t) {
                e.showToast({ title: "文件解析失败", icon: "none" }),
                  console.error("文件解析失败:", t);
              }
            },
            fail: function (t) {
              e.showToast({ title: "文件读取失败", icon: "none" }),
                console.error("文件读取失败:", t);
            },
          });
        };
        t.getparameter_setting = function () {
          var e;
          return [
            [
              {
                name: (0, o.t)("ble_page.parameter.bus_current"),
                type: "uint",
                value: 10,
                unit: "A",
                min: 0,
                max: 60,
                index: 0,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.phase_line_current"),
                type: "uint",
                value: 10,
                unit: "A",
                min: 0,
                max: 200,
                index: 1,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
            ],
            [
              {
                name: (0, o.t)("ble_page.parameter.motor_direction"),
                type: "bool",
                value: 1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.reverse"),
                offText: (0, o.t)("ble_page.choice.forward"),
                min: 0,
                max: 1,
                index: 2,
                bit_start: 0,
                bit_end: 0,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.position_sensor_type"),
                type: "list",
                options: ["120°HALL", "60°HALL", "编码器/encoder"],
                value: 1,
                unit: "",
                onText: "",
                offText: "1",
                min: 0,
                max: 2,
                index: 30,
                bit0: 0,
                bit1: 1,
                bit2: -1,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.hall_sequence"),
                type: "bool",
                value: 1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.reverseSequence"),
                offText: (0, o.t)("ble_page.choice.positiveSequence"),
                min: 0,
                max: 1,
                index: 31,
                bit_start: 0,
                bit_end: 0,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.encoder_sequence"),
                type: "bool",
                value: 1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.reverseSequence"),
                offText: (0, o.t)("ble_page.choice.positiveSequence"),
                min: 0,
                max: 1,
                index: 31,
                bit_start: 1,
                bit_end: 1,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.phase_shift_angle"),
                type: "int",
                value: -120,
                unit: "°",
                min: -180,
                max: 180,
                index: 32,
                bit_start: 0,
                bit_end: 15,
                scale: 182.0444444444444,
              },
              {
                name: (0, o.t)("ble_page.parameter.pole_pairs"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 50,
                index: 34,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.carrier_frequency"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 20,
                index: 48,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
                ver: 20,
              },
              {
                name: (0, o.t)("ble_page.parameter.reference_current"),
                type: "uint",
                value: 10,
                unit: "A",
                min: 0,
                max: 4095,
                index: 49,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)(
                  "ble_page.parameter.high_speed_gear_weak_magnetic_current"
                ),
                type: "uint",
                value: 10,
                unit: "A",
                min: 0,
                max: 500,
                index: 21,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.encoder_accuracy"),
                type: "uint",
                value: 0,
                unit: "线",
                min: 0,
                max: 8192,
                index: 51,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: "温度传感器类型",
                type: "uint",
                value: 0,
                unit: " ",
                min: 0,
                max: 255,
                index: 30,
                bit_start: 8,
                bit_end: 15,
                scale: 1,
                ver: 27,
              },
              {
                name: "降载温度",
                type: "uint",
                value: 0,
                unit: "度",
                min: 0,
                max: 255,
                index: 33,
                bit_start: 0,
                bit_end: 7,
                scale: 1,
                ver: 27,
              },
              {
                name: "过温温度",
                type: "uint",
                value: 0,
                unit: "度",
                min: 0,
                max: 255,
                index: 33,
                bit_start: 8,
                bit_end: 15,
                scale: 1,
                ver: 27,
              },
            ],
            [
              {
                name: (0, o.t)("ble_page.parameter.full_throttle_voltage"),
                type: "float",
                value: 0,
                unit: "V",
                min: 0,
                max: 5,
                index: 3,
                bit_start: 0,
                bit_end: 15,
                scale: 2450,
              },
              {
                name: (0, o.t)("ble_page.parameter.idle_throttle_voltage"),
                type: "float",
                value: 0,
                unit: "V",
                min: 0,
                max: 5,
                index: 4,
                bit_start: 0,
                bit_end: 15,
                scale: 2450,
              },
              {
                name: (0, o.t)("ble_page.parameter.forward_gear"),
                type: "float",
                value: 10,
                unit: "Sec",
                min: 0,
                max: 20,
                index: 5,
                bit_start: 0,
                bit_end: 15,
                scale: 273.0666667,
              },
              {
                name: (0, o.t)("ble_page.parameter.riding_mode"),
                type: "list",
                options: [
                  (0, o.t)("ble_page.choice.linear"),
                  (0, o.t)("ble_page.choice.softStart"),
                  (0, o.t)("ble_page.choice.motion"),
                  (0, o.t)("ble_page.choice.violent"),
                ],
                value: 0,
                unit: "",
                min: 0,
                max: 3,
                index: 29,
                bit0: 0,
                bit1: 14,
                bit2: -1,
                scale: 1,
              },
              {
                name: "刹车油门复位",
                type: "bool",
                value: 0,
                unit: "",
                onText: (0, o.t)("ble_page.choice.open"),
                offText: (0, o.t)("ble_page.choice.close"),
                min: 0,
                max: 1,
                index: 8,
                bit_start: 4,
                bit_end: 4,
                scale: 1,
                ver: 27,
              },
              {
                name: "刹把故障检测",
                type: "bool",
                value: 0,
                unit: "",
                onText: (0, o.t)("ble_page.choice.open"),
                offText: (0, o.t)("ble_page.choice.close"),
                min: 0,
                max: 1,
                index: 8,
                bit_start: 5,
                bit_end: 5,
                scale: 1,
                ver: 27,
              },
            ],
            [
              {
                name: (0, o.t)("ble_page.parameter.anti_theft_lock_motor"),
                type: "bool",
                reverse: !0,
                value: 1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.close"),
                offText: (0, o.t)("ble_page.choice.open"),
                min: 0,
                max: 1,
                index: 29,
                bit_start: 1,
                bit_end: 1,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.lock_motor_mode"),
                type: "bool",
                value: 1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.force"),
                offText: (0, o.t)("ble_page.choice.energySaving"),
                min: 0,
                max: 1,
                index: 29,
                bit_start: 11,
                bit_end: 11,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.auto_parking"),
                type: "bool",
                value: 1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.open"),
                offText: (0, o.t)("ble_page.choice.close"),
                min: 0,
                max: 1,
                index: 29,
                bit_start: 10,
                bit_end: 10,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.parking_signal"),
                type: "bool",
                value: 1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.lowBrake"),
                offText: (0, o.t)("ble_page.choice.highBrake"),
                min: 0,
                max: 1,
                index: 29,
                bit_start: 12,
                bit_end: 12,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.brake_driver"),
                type: "bool",
                value: 1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.open"),
                offText: (0, o.t)("ble_page.choice.close"),
                min: 0,
                max: 1,
                index: 29,
                bit_start: 15,
                bit_end: 15,
                scale: 1,
                ver: 26,
              },
              {
                name: (0, o.t)("ble_page.parameter.side_stand_effective"),
                type: "bool",
                value: !1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.open"),
                offText: (0, o.t)("ble_page.choice.close"),
                min: 0,
                max: 1,
                index: 29,
                bit_start: 7,
                bit_end: 7,
                scale: 1,
              },
              {
                name: (0, o.t)(
                  "ble_page.parameter.long_press_cruise_to_reverse"
                ),
                type: "bool",
                value: 1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.open"),
                offText: (0, o.t)("ble_page.choice.close"),
                min: 0,
                max: 1,
                index: 29,
                bit_start: 6,
                bit_end: 6,
                scale: 1,
              },
              ((e = {
                name: (0, o.t)("ble_page.parameter.warning_function"),
                type: "list",
                options: [
                  (0, o.t)("ble_page.choice.invalid"),
                  (0, o.t)("ble_page.choice.close"),
                  (0, o.t)("ble_page.choice.open"),
                ],
                value: 0,
                unit: "",
                min: 0,
                max: 3,
                index: 29,
                bit0: 8,
                bit1: 9,
                bit2: -1,
                mapping: { 0: 0, 1: 1, 3: 2 },
              }),
              (0, i.default)(e, "bit2", -1),
              (0, i.default)(e, "scale", 1),
              e),
              {
                name: (0, o.t)("ble_page.parameter.TCS_sensitivity"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 255,
                index: 58,
                bit_start: 0,
                bit_end: 7,
                scale: 1,
              },
              {
                name: "TCS恢复时间",
                type: "uint",
                value: 2,
                unit: "",
                min: 0,
                max: 15,
                index: 58,
                bit_start: 8,
                bit_end: 12,
                scale: 1,
                ver: 26,
              },
            ],
            [
              {
                name: (0, o.t)("ble_page.parameter.signal_port_selection"),
                type: "list",
                options: [
                  (0, o.t)("ble_page.choice.default"),
                  (0, o.t)("ble_page.choice.lowBrake"),
                  (0, o.t)("ble_page.choice.highBrake"),
                  (0, o.t)("ble_page.choice.cruise"),
                ],
                value: 1,
                unit: "",
                min: 0,
                max: 3,
                index: 29,
                bit0: 2,
                bit1: 3,
                bit2: 4,
                mapping: { 0: 0, 1: 1, 2: 2, 4: 3 },
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.trigger_mode"),
                type: "bool",
                value: 10,
                unit: "",
                onText: (0, o.t)("ble_page.choice.toggle"),
                offText: (0, o.t)("ble_page.choice.jog"),
                min: 0,
                max: 1,
                index: 29,
                bit_start: 5,
                bit_end: 5,
                scale: 1,
              },
              {
                name: (0, o.t)(
                  "ble_page.parameter.conventional_brake_release_p"
                ),
                type: "bool",
                value: 1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.open"),
                offText: (0, o.t)("ble_page.choice.close"),
                min: 0,
                max: 1,
                index: 29,
                bit_start: 13,
                bit_end: 13,
                scale: 1,
              },
              {
                name: "自动回P时间",
                type: "uint",
                value: 10,
                unit: "秒",
                min: 0,
                max: 255,
                index: 28,
                bit_start: 8,
                bit_end: 15,
                scale: 1,
                ver: 27,
              },
            ],
            [
              {
                name: (0, o.t)("ble_page.parameter.model_selection"),
                type: "list",
                options: [],
                mapping: {},
                description: [],
                value: 0,
                unit: "",
                min: 0,
                max: 5,
                index: 20,
                bit_start: 8,
                bit_end: 15,
                continuebit: !0,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.one_line_coefficient"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 255,
                index: 7,
                bit_start: 0,
                bit_end: 7,
                scale: 1,
              },
              {
                name: "一线通协议",
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 50,
                index: 7,
                bit_start: 8,
                bit_end: 15,
                scale: 1,
                ver: 27,
              },
              {
                name: (0, o.t)("ble_page.parameter.hall_meter_pulse_count"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 500,
                index: 8,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
                ver: 16,
              },
              {
                name: (0, o.t)("ble_page.parameter.bms_protocol"),
                type: "bool",
                value: 1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.open"),
                offText: (0, o.t)("ble_page.choice.close"),
                min: 0,
                max: 1,
                index: 8,
                bit_start: 0,
                bit_end: 0,
                scale: 1,
                ver: 27,
              },
              {
                name: "端口功能",
                type: "list",
                options: ["一线通输出", "霍尔脉冲", "保留", "保留", "灯控"],
                value: 0,
                unit: "",
                min: 0,
                max: 4,
                index: 8,
                bit0: 1,
                bit1: 2,
                bit2: 3,
                mapping: { 0: 0, 1: 1, 2: 2, 3: 3, 4: 4 },
                scale: 1,
                ver: 27,
              },
            ],
            [
              {
                name: (0, o.t)("ble_page.parameter.high_speed_gear_max_speed"),
                type: "uint",
                value: 10,
                unit: "rpm",
                min: 0,
                max: 6e3,
                index: 11,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
                border: !0,
              },
              {
                name: (0, o.t)(
                  "ble_page.parameter.high_speed_gear_phase_line_current"
                ),
                type: "uint",
                value: 10,
                unit: "%",
                min: 0,
                max: 100,
                index: 14,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
                border: !0,
              },
              {
                name: (0, o.t)(
                  "ble_page.parameter.high_speed_gear_bus_current"
                ),
                type: "uint",
                value: 10,
                unit: "%",
                min: 0,
                max: 100,
                index: 17,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
                divider: !0,
                border: !0,
              },
              {
                name: (0, o.t)(
                  "ble_page.parameter.medium_speed_gear_max_speed"
                ),
                type: "uint",
                value: 10,
                unit: "rpm",
                min: 0,
                max: 6e3,
                index: 12,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
                border: !0,
              },
              {
                name: (0, o.t)(
                  "ble_page.parameter.medium_speed_gear_phase_line_current"
                ),
                type: "uint",
                value: 10,
                unit: "%",
                min: 0,
                max: 100,
                index: 15,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
                border: !0,
              },
              {
                name: (0, o.t)(
                  "ble_page.parameter.medium_speed_gear_bus_current"
                ),
                type: "uint",
                value: 10,
                unit: "%",
                min: 0,
                max: 100,
                index: 18,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
                divider: !0,
                border: !0,
              },
              {
                name: (0, o.t)("ble_page.parameter.low_speed_gear_max_speed"),
                type: "uint",
                value: 10,
                unit: "rpm",
                min: 0,
                max: 6e3,
                index: 13,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
                border: !0,
              },
              {
                name: (0, o.t)(
                  "ble_page.parameter.low_speed_gear_phase_line_current"
                ),
                type: "uint",
                value: 10,
                unit: "%",
                min: 0,
                max: 100,
                index: 16,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
                border: !0,
              },
              {
                name: (0, o.t)("ble_page.parameter.low_speed_gear_bus_current"),
                type: "uint",
                value: 10,
                unit: "%",
                min: 0,
                max: 100,
                index: 19,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
                divider: !0,
                border: !0,
              },
              {
                name: (0, o.t)("ble_page.parameter.shift_method"),
                type: "list",
                options: [
                  (0, o.t)("ble_page.choice.toggle"),
                  (0, o.t)("ble_page.choice.jog"),
                  (0, o.t)("ble_page.choice.addsub"),
                ],
                value: 0,
                unit: "",
                min: 0,
                max: 2,
                index: 20,
                bit0: 0,
                bit1: 1,
                bit2: -1,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.momentary_default_gear"),
                type: "list",
                options: [
                  (0, o.t)("ble_page.choice.lowSpeedGear"),
                  (0, o.t)("ble_page.choice.mediumSpeedGear"),
                  (0, o.t)("ble_page.choice.highSpeedGear"),
                ],
                value: 0,
                unit: "",
                min: 0,
                max: 3,
                index: 20,
                bit0: 4,
                bit1: 5,
                bit2: -1,
                scale: 1,
              },
            ],
            [
              {
                name: (0, o.t)("ble_page.parameter.reverse_gear"),
                type: "float",
                value: 10,
                unit: "Sec",
                min: 0,
                max: 20,
                index: 6,
                bit_start: 0,
                bit_end: 15,
                scale: 273.0666667,
              },
              {
                name: (0, o.t)("ble_page.parameter.reverse_speed"),
                type: "uint",
                value: 10,
                unit: "rpm",
                min: 0,
                max: 1e3,
                index: 25,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.reverse_torque"),
                type: "uint",
                value: 10,
                unit: "N·m",
                min: 0,
                max: 100,
                index: 28,
                bit_start: 0,
                bit_end: 7,
                scale: 1,
              },
            ],
            [
              {
                name: (0, o.t)("ble_page.parameter.under_voltage_point"),
                type: "uint",
                value: 10,
                unit: "V",
                min: 0,
                max: 130,
                index: 9,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.over_voltage_point"),
                type: "uint",
                value: 10,
                unit: "V",
                min: 0,
                max: 130,
                index: 10,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)(
                  "ble_page.parameter.power_reduction_start_voltage"
                ),
                type: "uint",
                value: 10,
                unit: "V",
                min: 0,
                max: 100,
                index: 27,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)(
                  "ble_page.parameter.power_reduction_end_voltage"
                ),
                type: "uint",
                value: 10,
                unit: "V",
                min: 0,
                max: 100,
                index: 26,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
            ],
            [
              {
                name: (0, o.t)("ble_page.parameter.battery_series"),
                type: "uint",
                value: 10,
                unit: "S",
                min: 0,
                max: 63,
                index: 46,
                bit_start: 0,
                bit_end: 5,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.battery_capacity"),
                type: "uint",
                value: 10,
                unit: "Ah",
                min: 0,
                max: 1023,
                index: 46,
                bit_start: 6,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.discharge_coefficient"),
                type: "uint",
                value: 10,
                unit: " ",
                min: 0,
                max: 255,
                index: 59,
                bit_start: 0,
                bit_end: 7,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.battery_type"),
                type: "list",
                options: [
                  (0, o.t)("ble_page.choice.ternaryLithium"),
                  (0, o.t)("ble_page.choice.lithiumIronPhosphate"),
                  (0, o.t)("ble_page.choice.leadAcidBattery"),
                  (0, o.t)("ble_page.choice.lithiumManganate"),
                ],
                value: 0,
                unit: "",
                min: 0,
                max: 3,
                index: 59,
                bit0: 8,
                bit1: 9,
                bit2: -1,
                scale: 1,
              },
            ],
            [
              {
                name: (0, o.t)("ble_page.parameter.brake_feedback_torque"),
                type: "uint",
                value: 10,
                unit: "N·m",
                min: 0,
                max: 100,
                index: 22,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)(
                  "ble_page.parameter.release_throttle_feedback_torque"
                ),
                type: "uint",
                value: 10,
                unit: "N·m",
                min: 0,
                max: 100,
                index: 45,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.regenerative_current"),
                type: "uint",
                value: 10,
                unit: "A",
                min: 0,
                max: 100,
                index: 23,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.minimum_feedback_speed"),
                type: "uint",
                value: 10,
                unit: "rpm",
                min: 0,
                max: 3e3,
                index: 24,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.regenerative_voltage_limit"),
                type: "uint",
                value: 10,
                unit: "V",
                min: 0,
                max: 100,
                index: 57,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: "陡坡缓降",
                type: "text",
                value: 10,
                unit: " ",
                min: 0,
                max: 100,
                index: 23,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
                ver: 26,
              },
            ],
            [
              {
                name: (0, o.t)("ble_page.parameter.C_Kp"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 35,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.C_Ki"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 36,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.S_Kp"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 43,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.S_Ki"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 44,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.LF_parameter"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 53,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.SF_parameter"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 54,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.utilization"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 37,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.margin"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 38,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.adjustment_coefficient"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 39,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.back_EMF"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 40,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.rated_speed"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 41,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.reference_1"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 55,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
              {
                name: (0, o.t)("ble_page.parameter.reference_2"),
                type: "uint",
                value: 10,
                unit: "",
                min: 0,
                max: 4095,
                index: 56,
                bit_start: 0,
                bit_end: 15,
                scale: 1,
              },
            ],
          ];
        };
      }).call(
        this,
        n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/wx.js */ 1).default
      );
    },
    /*!**********************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/utils/hardwareConfig.js ***!
  \**********************************************************************/
    /*! no static exports found */ function (e, t, n) {
      Object.defineProperty(t, "__esModule", { value: !0 }),
        (t.default = void 0);
      var r = {
        2: {
          subSeries: {
            1: { subSeries: { 3: { name: "7235", ibus: 35, iphase: 140 } } },
            2: {
              subSeries: {
                3: { name: "72260", ibus: 80, iphase: 260 },
                2: { name: "7260S", ibus: 60, iphase: 240 },
                1: { name: "72240", ibus: 70, iphase: 240 },
              },
            },
            3: {
              subSeries: {
                1: { name: "72300", ibus: 100, iphase: 300 },
                3: { name: "72350", ibus: 120, iphase: 350 },
              },
            },
            4: {
              subSeries: {
                1: { name: "72400", ibus: 150, iphase: 400 },
                3: { name: "72450", ibus: 200, iphase: 450 },
              },
            },
            5: {
              subSeries: {
                1: { name: "72500", ibus: 200, iphase: 500 },
                2: { name: "72530", ibus: 250, iphase: 530 },
                3: { name: "72550", ibus: 250, iphase: 550 },
              },
            },
            6: { subSeries: { 3: { name: "72650", ibus: 300, iphase: 650 } } },
            7: { subSeries: { 2: { name: "72550P", ibus: 350, iphase: 800 } } },
            8: {
              subSeries: {
                3: { name: "72850", ibus: 400, iphase: 850 },
                4: { name: "721000", ibus: 500, iphase: 1e3 },
              },
            },
            9: {
              subSeries: {
                1: { name: "721200", ibus: 600, iphase: 1200 },
                2: { name: "721600", ibus: 700, iphase: 1600 },
                3: { name: "722000", ibus: 800, iphase: 2e3 },
              },
            },
          },
        },
        4: {
          subSeries: {
            3: {
              subSeries: {
                2: { name: "72350", ibus: 120, iphase: 350 },
                3: { name: "72380", ibus: 150, iphase: 380 },
              },
            },
          },
        },
        6: {
          subSeries: {
            8: {
              subSeries: {
                3: { name: "84850", ibus: 400, iphase: 850 },
                4: { name: "841000", ibus: 500, iphase: 1e3 },
              },
            },
            9: {
              subSeries: { 1: { name: "841200", ibus: 600, iphase: 1200 } },
            },
          },
        },
      };
      t.default = r;
    },
    /*!**********************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/utils/parametertable.js ***!
  \**********************************************************************/
    /*! no static exports found */ function (e, t, n) {
      var r = n(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
      Object.defineProperty(t, "__esModule", { value: !0 }),
        (t.getParameterSetting = void 0);
      var a,
        i = r(n(/*! @babel/runtime/helpers/defineProperty */ 11)),
        o = n(/*! @/utils/i18n.js */ 40);
      function s(e, t) {
        var n = Object.keys(e);
        if (Object.getOwnPropertySymbols) {
          var r = Object.getOwnPropertySymbols(e);
          t &&
            (r = r.filter(function (t) {
              return Object.getOwnPropertyDescriptor(e, t).enumerable;
            })),
            n.push.apply(n, r);
        }
        return n;
      }
      function c(e) {
        for (var t = 1; t < arguments.length; t++) {
          var n = null != arguments[t] ? arguments[t] : {};
          t % 2
            ? s(Object(n), !0).forEach(function (t) {
                (0, i.default)(e, t, n[t]);
              })
            : Object.getOwnPropertyDescriptors
            ? Object.defineProperties(e, Object.getOwnPropertyDescriptors(n))
            : s(Object(n)).forEach(function (t) {
                Object.defineProperty(
                  e,
                  t,
                  Object.getOwnPropertyDescriptor(n, t)
                );
              });
        }
        return e;
      }
      var u = [
          [
            {
              name: (0, o.t)("ble_page.parameter.bus_current"),
              type: "uint",
              value: 10,
              unit: "A",
              min: 0,
              max: 60,
              index: 0,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.phase_line_current"),
              type: "uint",
              value: 10,
              unit: "A",
              min: 0,
              max: 200,
              index: 1,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
          ],
          [
            {
              name: (0, o.t)("ble_page.parameter.motor_direction"),
              type: "bool",
              value: 1,
              unit: "",
              onText: (0, o.t)("ble_page.choice.reverse"),
              offText: (0, o.t)("ble_page.choice.forward"),
              min: 0,
              max: 1,
              index: 2,
              bit_start: 0,
              bit_end: 0,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.position_sensor_type"),
              type: "list",
              options: ["120°HALL", "60°HALL", "编码器/encoder"],
              value: 1,
              unit: "",
              onText: "",
              offText: "1",
              min: 0,
              max: 2,
              index: 30,
              bit0: 0,
              bit1: 1,
              bit2: -1,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.hall_sequence"),
              type: "bool",
              value: 1,
              unit: "",
              onText: (0, o.t)("ble_page.choice.reverseSequence"),
              offText: (0, o.t)("ble_page.choice.positiveSequence"),
              min: 0,
              max: 1,
              index: 31,
              bit_start: 0,
              bit_end: 0,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.encoder_sequence"),
              type: "bool",
              value: 1,
              unit: "",
              onText: (0, o.t)("ble_page.choice.reverseSequence"),
              offText: (0, o.t)("ble_page.choice.positiveSequence"),
              min: 0,
              max: 1,
              index: 31,
              bit_start: 1,
              bit_end: 1,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.phase_shift_angle"),
              type: "int",
              value: -120,
              unit: "°",
              min: -180,
              max: 180,
              index: 32,
              bit_start: 0,
              bit_end: 15,
              scale: 182.0444444444444,
            },
            {
              name: (0, o.t)("ble_page.parameter.pole_pairs"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 50,
              index: 34,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.carrier_frequency"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 20,
              index: 48,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
              ver: 20,
            },
            {
              name: (0, o.t)("ble_page.parameter.reference_current"),
              type: "uint",
              value: 10,
              unit: "A",
              min: 0,
              max: 4095,
              index: 49,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)(
                "ble_page.parameter.high_speed_gear_weak_magnetic_current"
              ),
              type: "uint",
              value: 10,
              unit: "A",
              min: 0,
              max: 500,
              index: 21,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.encoder_accuracy"),
              type: "uint",
              value: 0,
              unit: "线",
              min: 0,
              max: 8192,
              index: 51,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
          ],
          [
            {
              name: (0, o.t)("ble_page.parameter.full_throttle_voltage"),
              type: "float",
              value: 0,
              unit: "V",
              min: 0,
              max: 5,
              index: 3,
              bit_start: 0,
              bit_end: 15,
              scale: 2450,
            },
            {
              name: (0, o.t)("ble_page.parameter.idle_throttle_voltage"),
              type: "float",
              value: 0,
              unit: "V",
              min: 0,
              max: 5,
              index: 4,
              bit_start: 0,
              bit_end: 15,
              scale: 2450,
            },
            {
              name: (0, o.t)("ble_page.parameter.forward_gear"),
              type: "float",
              value: 10,
              unit: "Sec",
              min: 0,
              max: 20,
              index: 5,
              bit_start: 0,
              bit_end: 15,
              scale: 273.0666667,
            },
            {
              name: (0, o.t)("ble_page.parameter.riding_mode"),
              type: "list",
              options: [
                (0, o.t)("ble_page.choice.linear"),
                (0, o.t)("ble_page.choice.softStart"),
                (0, o.t)("ble_page.choice.motion"),
                (0, o.t)("ble_page.choice.violent"),
              ],
              value: 0,
              unit: "",
              min: 0,
              max: 3,
              index: 29,
              bit0: 0,
              bit1: 14,
              bit2: -1,
              scale: 1,
            },
          ],
          [
            {
              name: (0, o.t)("ble_page.parameter.anti_theft_lock_motor"),
              type: "bool",
              en: !0,
              reverse: !0,
              value: 1,
              unit: "",
              onText: (0, o.t)("ble_page.choice.close"),
              offText: (0, o.t)("ble_page.choice.open"),
              min: 0,
              max: 1,
              index: 29,
              bit_start: 1,
              bit_end: 1,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.lock_motor_mode"),
              type: "bool",
              en: !0,
              value: 1,
              unit: "",
              onText: (0, o.t)("ble_page.choice.force"),
              offText: (0, o.t)("ble_page.choice.energySaving"),
              min: 0,
              max: 1,
              index: 29,
              bit_start: 11,
              bit_end: 11,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.auto_parking"),
              type: "bool",
              en: !0,
              value: 1,
              unit: "",
              onText: (0, o.t)("ble_page.choice.open"),
              offText: (0, o.t)("ble_page.choice.close"),
              min: 0,
              max: 1,
              index: 29,
              bit_start: 10,
              bit_end: 10,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.parking_signal"),
              type: "bool",
              en: !0,
              value: 1,
              unit: "",
              onText: (0, o.t)("ble_page.choice.lowBrake"),
              offText: (0, o.t)("ble_page.choice.highBrake"),
              min: 0,
              max: 1,
              index: 29,
              bit_start: 12,
              bit_end: 12,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.brake_driver"),
              type: "bool",
              en: !0,
              value: 1,
              unit: "",
              onText: (0, o.t)("ble_page.choice.open"),
              offText: (0, o.t)("ble_page.choice.close"),
              min: 0,
              max: 1,
              index: 29,
              bit_start: 15,
              bit_end: 15,
              scale: 1,
              ver: 26,
            },
            {
              name: (0, o.t)("ble_page.parameter.side_stand_effective"),
              type: "bool",
              en: !0,
              value: !1,
              unit: "",
              onText: (0, o.t)("ble_page.choice.open"),
              offText: (0, o.t)("ble_page.choice.close"),
              min: 0,
              max: 1,
              index: 29,
              bit_start: 7,
              bit_end: 7,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.long_press_cruise_to_reverse"),
              type: "bool",
              en: !0,
              value: 1,
              unit: "",
              onText: (0, o.t)("ble_page.choice.open"),
              offText: (0, o.t)("ble_page.choice.close"),
              min: 0,
              max: 1,
              index: 29,
              bit_start: 6,
              bit_end: 6,
              scale: 1,
            },
            ((a = {
              name: (0, o.t)("ble_page.parameter.warning_function"),
              type: "list",
              options: [
                (0, o.t)("ble_page.choice.invalid"),
                (0, o.t)("ble_page.choice.close"),
                (0, o.t)("ble_page.choice.open"),
              ],
              value: 0,
              unit: "",
              min: 0,
              max: 3,
              index: 29,
              bit0: 8,
              bit1: 9,
              bit2: -1,
              mapping: { 0: 0, 1: 1, 3: 2 },
            }),
            (0, i.default)(a, "bit2", -1),
            (0, i.default)(a, "scale", 1),
            a),
            {
              name: (0, o.t)("ble_page.parameter.TCS_sensitivity"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 255,
              index: 58,
              bit_start: 0,
              bit_end: 7,
              scale: 1,
            },
            {
              name: "TCS恢复时间",
              type: "uint",
              value: 2,
              unit: "",
              min: 0,
              max: 15,
              index: 58,
              bit_start: 8,
              bit_end: 12,
              scale: 1,
              ver: 26,
            },
          ],
          [
            {
              name: (0, o.t)("ble_page.parameter.signal_port_selection"),
              type: "list",
              options: [
                (0, o.t)("ble_page.choice.default"),
                (0, o.t)("ble_page.choice.lowBrake"),
                (0, o.t)("ble_page.choice.highBrake"),
                (0, o.t)("ble_page.choice.cruise"),
              ],
              value: 1,
              unit: "",
              min: 0,
              max: 3,
              index: 29,
              bit0: 2,
              bit1: 3,
              bit2: 4,
              mapping: { 0: 0, 1: 1, 2: 2, 4: 3 },
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.trigger_mode"),
              type: "bool",
              en: !1,
              value: 10,
              unit: "",
              onText: (0, o.t)("ble_page.choice.toggle"),
              offText: (0, o.t)("ble_page.choice.jog"),
              min: 0,
              max: 1,
              index: 29,
              bit_start: 5,
              bit_end: 5,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.conventional_brake_release_p"),
              type: "bool",
              en: !1,
              value: 1,
              unit: "",
              onText: (0, o.t)("ble_page.choice.open"),
              offText: (0, o.t)("ble_page.choice.close"),
              min: 0,
              max: 1,
              index: 29,
              bit_start: 13,
              bit_end: 13,
              scale: 1,
            },
          ],
          [
            {
              name: (0, o.t)("ble_page.parameter.model_selection"),
              type: "list",
              options: [],
              mapping: {},
              description: [],
              value: 0,
              unit: "",
              min: 0,
              max: 5,
              index: 20,
              bit_start: 8,
              bit_end: 15,
              continuebit: !0,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.one_line_coefficient"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 255,
              index: 7,
              bit_start: 0,
              bit_end: 7,
              scale: 1,
            },
            {
              name: "一线通协议",
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 50,
              index: 7,
              bit_start: 8,
              bit_end: 15,
              scale: 1,
              ver: 26,
            },
            {
              name: (0, o.t)("ble_page.parameter.hall_meter_pulse_count"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 500,
              index: 8,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
              ver: 16,
            },
          ],
          [
            {
              name: (0, o.t)("ble_page.parameter.high_speed_gear_max_speed"),
              type: "uint",
              value: 10,
              unit: "rpm",
              min: 0,
              max: 5e3,
              index: 11,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
              border: !0,
            },
            {
              name: (0, o.t)(
                "ble_page.parameter.high_speed_gear_phase_line_current"
              ),
              type: "uint",
              value: 10,
              unit: "%",
              min: 0,
              max: 100,
              index: 14,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
              border: !0,
            },
            {
              name: (0, o.t)("ble_page.parameter.high_speed_gear_bus_current"),
              type: "uint",
              value: 10,
              unit: "%",
              min: 0,
              max: 100,
              index: 17,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
              divider: !0,
              border: !0,
            },
            {
              name: (0, o.t)("ble_page.parameter.medium_speed_gear_max_speed"),
              type: "uint",
              value: 10,
              unit: "rpm",
              min: 0,
              max: 5e3,
              index: 12,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
              border: !0,
            },
            {
              name: (0, o.t)(
                "ble_page.parameter.medium_speed_gear_phase_line_current"
              ),
              type: "uint",
              value: 10,
              unit: "%",
              min: 0,
              max: 100,
              index: 15,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
              border: !0,
            },
            {
              name: (0, o.t)(
                "ble_page.parameter.medium_speed_gear_bus_current"
              ),
              type: "uint",
              value: 10,
              unit: "%",
              min: 0,
              max: 100,
              index: 18,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
              divider: !0,
              border: !0,
            },
            {
              name: (0, o.t)("ble_page.parameter.low_speed_gear_max_speed"),
              type: "uint",
              value: 10,
              unit: "rpm",
              min: 0,
              max: 5e3,
              index: 13,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
              border: !0,
            },
            {
              name: (0, o.t)(
                "ble_page.parameter.low_speed_gear_phase_line_current"
              ),
              type: "uint",
              value: 10,
              unit: "%",
              min: 0,
              max: 100,
              index: 16,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
              border: !0,
            },
            {
              name: (0, o.t)("ble_page.parameter.low_speed_gear_bus_current"),
              type: "uint",
              value: 10,
              unit: "%",
              min: 0,
              max: 100,
              index: 19,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
              divider: !0,
              border: !0,
            },
            {
              name: (0, o.t)("ble_page.parameter.shift_method"),
              type: "list",
              options: [
                (0, o.t)("ble_page.choice.toggle"),
                (0, o.t)("ble_page.choice.jog"),
                (0, o.t)("ble_page.choice.addsub"),
              ],
              value: 0,
              unit: "",
              min: 0,
              max: 2,
              index: 20,
              bit0: 0,
              bit1: 1,
              bit2: -1,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.momentary_default_gear"),
              type: "list",
              options: [
                (0, o.t)("ble_page.choice.lowSpeedGear"),
                (0, o.t)("ble_page.choice.mediumSpeedGear"),
                (0, o.t)("ble_page.choice.highSpeedGear"),
              ],
              value: 0,
              unit: "",
              min: 0,
              max: 3,
              index: 20,
              bit0: 4,
              bit1: 5,
              bit2: -1,
              scale: 1,
            },
          ],
          [
            {
              name: (0, o.t)("ble_page.parameter.reverse_gear"),
              type: "float",
              value: 10,
              unit: "Sec",
              min: 0,
              max: 20,
              index: 6,
              bit_start: 0,
              bit_end: 15,
              scale: 273.0666667,
            },
            {
              name: (0, o.t)("ble_page.parameter.reverse_speed"),
              type: "uint",
              value: 10,
              unit: "rpm",
              min: 0,
              max: 1e3,
              index: 25,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.reverse_torque"),
              type: "uint",
              value: 10,
              unit: "N·m",
              min: 0,
              max: 100,
              index: 28,
              bit_start: 0,
              bit_end: 7,
              scale: 1,
            },
          ],
          [
            {
              name: (0, o.t)("ble_page.parameter.under_voltage_point"),
              type: "uint",
              value: 10,
              unit: "V",
              min: 0,
              max: 110,
              index: 9,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.over_voltage_point"),
              type: "uint",
              value: 10,
              unit: "V",
              min: 0,
              max: 110,
              index: 10,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)(
                "ble_page.parameter.power_reduction_start_voltage"
              ),
              type: "uint",
              value: 10,
              unit: "V",
              min: 0,
              max: 110,
              index: 27,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.power_reduction_end_voltage"),
              type: "uint",
              value: 10,
              unit: "V",
              min: 0,
              max: 110,
              index: 26,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
          ],
          [
            {
              name: (0, o.t)("ble_page.parameter.battery_series"),
              type: "uint",
              value: 10,
              unit: "S",
              min: 0,
              max: 63,
              index: 46,
              bit_start: 0,
              bit_end: 5,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.battery_capacity"),
              type: "uint",
              value: 10,
              unit: "Ah",
              min: 0,
              max: 1023,
              index: 46,
              bit_start: 6,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.discharge_coefficient"),
              type: "uint",
              value: 10,
              unit: " ",
              min: 0,
              max: 255,
              index: 59,
              bit_start: 0,
              bit_end: 7,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.battery_type"),
              type: "list",
              options: [
                (0, o.t)("ble_page.choice.ternaryLithium"),
                (0, o.t)("ble_page.choice.lithiumIronPhosphate"),
                (0, o.t)("ble_page.choice.leadAcidBattery"),
                (0, o.t)("ble_page.choice.lithiumManganate"),
              ],
              value: 0,
              unit: "",
              min: 0,
              max: 3,
              index: 59,
              bit0: 8,
              bit1: 9,
              bit2: -1,
              scale: 1,
            },
          ],
          [
            {
              name: (0, o.t)("ble_page.parameter.brake_feedback_torque"),
              type: "uint",
              value: 10,
              unit: "N·m",
              min: 0,
              max: 100,
              index: 22,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)(
                "ble_page.parameter.release_throttle_feedback_torque"
              ),
              type: "uint",
              value: 10,
              unit: "N·m",
              min: 0,
              max: 100,
              index: 45,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.regenerative_current"),
              type: "uint",
              value: 10,
              unit: "A",
              min: 0,
              max: 100,
              index: 23,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.minimum_feedback_speed"),
              type: "uint",
              value: 10,
              unit: "rpm",
              min: 0,
              max: 3e3,
              index: 24,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.regenerative_voltage_limit"),
              type: "uint",
              value: 10,
              unit: "V",
              min: 0,
              max: 110,
              index: 57,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: "陡坡缓降",
              type: "text",
              value: 10,
              unit: " ",
              min: 0,
              max: 100,
              index: 23,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
              ver: 26,
            },
          ],
          [
            {
              name: (0, o.t)("ble_page.parameter.C_Kp"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 35,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.C_Ki"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 36,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.S_Kp"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 43,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.S_Ki"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 44,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.LF_parameter"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 53,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.SF_parameter"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 54,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.utilization"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 37,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.margin"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 38,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.adjustment_coefficient"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 39,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.back_EMF"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 40,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.rated_speed"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 41,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.reference_1"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 55,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
            {
              name: (0, o.t)("ble_page.parameter.reference_2"),
              type: "uint",
              value: 10,
              unit: "",
              min: 0,
              max: 4095,
              index: 56,
              bit_start: 0,
              bit_end: 15,
              scale: 1,
            },
          ],
        ],
        l = {
          27: [
            {
              type: "add",
              groupIndex: 1,
              position: -1,
              newItem: {
                name: "温度传感器类型",
                type: "list",
                options: [
                  "关闭",
                  "Kty84130/150",
                  "Kty83122",
                  "NTC3435",
                  "NTC3950",
                ],
                value: 0,
                unit: " ",
                min: 0,
                max: 255,
                index: 30,
                bit_start: 8,
                bit_end: 15,
                scale: 1,
                continuebit: !0,
              },
            },
            {
              type: "add",
              groupIndex: 1,
              position: -1,
              newItem: {
                name: "降载温度",
                type: "uint",
                value: 0,
                unit: "度",
                min: 0,
                max: 255,
                index: 33,
                bit_start: 0,
                bit_end: 7,
                scale: 1,
              },
            },
            {
              type: "add",
              groupIndex: 1,
              position: -1,
              newItem: {
                name: "过温温度",
                type: "uint",
                value: 0,
                unit: "度",
                min: 0,
                max: 255,
                index: 33,
                bit_start: 8,
                bit_end: 15,
                scale: 1,
              },
            },
            {
              type: "add",
              groupIndex: 2,
              position: -1,
              newItem: {
                name: "刹车油门复位",
                type: "bool",
                value: 0,
                unit: "",
                onText: (0, o.t)("ble_page.choice.open"),
                offText: (0, o.t)("ble_page.choice.close"),
                min: 0,
                max: 1,
                index: 8,
                bit_start: 4,
                bit_end: 4,
                scale: 1,
              },
            },
            {
              type: "add",
              groupIndex: 2,
              position: -1,
              newItem: {
                name: "刹把故障检测",
                type: "bool",
                value: 0,
                unit: "",
                onText: (0, o.t)("ble_page.choice.open"),
                offText: (0, o.t)("ble_page.choice.close"),
                min: 0,
                max: 1,
                index: 8,
                bit_start: 5,
                bit_end: 5,
                scale: 1,
              },
            },
            {
              type: "add",
              groupIndex: 4,
              position: -1,
              newItem: {
                name: "自动回P时间",
                type: "uint",
                value: 10,
                unit: "秒",
                min: 0,
                max: 255,
                index: 28,
                bit_start: 8,
                bit_end: 15,
                scale: 1,
                ver: 27,
              },
            },
            { type: "delete", groupIndex: 5, itemIndex: 3 },
            {
              type: "add",
              groupIndex: 5,
              position: -1,
              newItem: {
                name: (0, o.t)("ble_page.parameter.bms_protocol"),
                type: "bool",
                value: 1,
                unit: "",
                onText: (0, o.t)("ble_page.choice.open"),
                offText: (0, o.t)("ble_page.choice.close"),
                min: 0,
                max: 1,
                index: 8,
                bit_start: 0,
                bit_end: 0,
                scale: 1,
                ver: 27,
              },
            },
            {
              type: "add",
              groupIndex: 5,
              position: -1,
              newItem: {
                name: "端口功能",
                type: "list",
                options: ["一线通输出", "霍尔脉冲", "保留", "保留", "灯控"],
                value: 0,
                unit: "",
                min: 0,
                max: 4,
                index: 8,
                bit0: 1,
                bit1: 2,
                bit2: 3,
                mapping: { 0: 0, 1: 1, 2: 2, 3: 3, 4: 4 },
                scale: 1,
                ver: 27,
              },
            },
            {
              type: "add",
              groupIndex: 11,
              position: -1,
              newItem: {
                name: "Ibus_Filter",
                type: "uint",
                value: 50,
                unit: "",
                min: 0,
                max: 255,
                index: 47,
                bit_start: 0,
                bit_end: 7,
                scale: 1,
                ver: 27,
              },
            },
            {
              type: "replace",
              groupIndex: 6,
              itemIndex: 0,
              replace: { max: 1e4 },
            },
            {
              type: "replace",
              groupIndex: 6,
              itemIndex: 3,
              replace: { max: 1e4 },
            },
            {
              type: "replace",
              groupIndex: 6,
              itemIndex: 6,
              replace: { max: 1e4 },
            },
          ],
        };
      t.getParameterSetting = function (e) {
        return e
          ? (function (e) {
              var t = JSON.parse(JSON.stringify(u));
              return (
                Object.keys(l)
                  .map(function (e) {
                    return Number(e);
                  })
                  .sort(function (e, t) {
                    return e - t;
                  })
                  .forEach(function (n) {
                    if (!(n > e)) {
                      var r = l[n];
                      r && 0 !== r.length
                        ? (console.log(
                            "开始应用版本"
                              .concat(n, "的补丁（共")
                              .concat(r.length, "个）")
                          ),
                          r.forEach(function (e, r) {
                            var a = e.type,
                              i = e.groupIndex,
                              o = t[i];
                            if (o)
                              if ("replace" === a) {
                                var s = e.itemIndex,
                                  u = e.replace;
                                if (s < 0 || s >= o.length)
                                  return void console.warn(
                                    "版本"
                                      .concat(n, "补丁")
                                      .concat(r, "（替换）失败：组")
                                      .concat(i, "内无索引")
                                      .concat(s, "的元素")
                                  );
                                (o[s] = c(c({}, o[s]), u)),
                                  console.log(
                                    "版本"
                                      .concat(n, "补丁")
                                      .concat(r, "（替换）完成：组")
                                      .concat(i, "内第")
                                      .concat(s, "个元素")
                                  );
                              } else if ("add" === a) {
                                var l = e.position,
                                  p = e.newItem,
                                  d = c(
                                    c(
                                      {},
                                      {
                                        name: "",
                                        type: "uint",
                                        value: 0,
                                        unit: "",
                                        min: 0,
                                        max: 0,
                                        index: 0,
                                        bit_start: 0,
                                        bit_end: 0,
                                        scale: 1,
                                        ver: n,
                                      }
                                    ),
                                    p
                                  ),
                                  f = -1 === l ? o.length : l;
                                if (f < 0 || f > o.length)
                                  return void console.warn(
                                    "版本"
                                      .concat(n, "补丁")
                                      .concat(r, "（新增）失败：组")
                                      .concat(i, "内插入位置")
                                      .concat(f, "不合法（组内共")
                                      .concat(o.length, "个元素）")
                                  );
                                o.splice(f, 0, d),
                                  console.log(
                                    "版本"
                                      .concat(n, "补丁")
                                      .concat(r, "（新增）完成：组")
                                      .concat(i, "内第")
                                      .concat(f, "个位置新增元素")
                                  );
                              } else if ("delete" === a) {
                                var _ = e.itemIndex;
                                if (_ < 0 || _ >= o.length)
                                  return void console.warn(
                                    "版本"
                                      .concat(n, "补丁")
                                      .concat(r, "（删除）失败：组")
                                      .concat(i, "内无索引")
                                      .concat(_, "的元素")
                                  );
                                var h = o.splice(_, 1)[0];
                                console.log(
                                  "版本"
                                    .concat(n, "补丁")
                                    .concat(r, "（删除）完成：组")
                                    .concat(i, "内第")
                                    .concat(_, "个元素【")
                                    .concat(h.name, "】已删除")
                                );
                              } else
                                console.warn(
                                  "版本"
                                    .concat(n, "补丁")
                                    .concat(r, "应用失败：未知操作类型")
                                    .concat(a, "（仅支持replace/add/delete）")
                                );
                            else
                              console.warn(
                                "版本"
                                  .concat(n, "补丁")
                                  .concat(r, "应用失败：组索引")
                                  .concat(i, "不存在")
                              );
                          }))
                        : console.log("版本".concat(n, "无补丁，跳过"));
                    }
                  }),
                console.log(
                  "版本"
                    .concat(e, "所有补丁应用完成（已叠加所有≤")
                    .concat(e, "的版本补丁）")
                ),
                t
              );
            })(e)
          : (console.warn("设备版本未传入，使用默认参数表"),
            JSON.parse(JSON.stringify(u)));
      };
    },
    /*!*****************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/utils/bluetooth.js ***!
  \*****************************************************************/
    /*! no static exports found */ function (e, t, n) {
      (function (e) {
        Object.defineProperty(t, "__esModule", { value: !0 }),
          (t.default = void 0);
        var n = !1,
          r = null,
          a = null,
          i = null,
          o = null,
          s = null,
          c = null,
          u = null,
          l = !1;
        function p(e) {
          return Array.prototype.map
            .call(new Uint8Array(e), function (e) {
              return ("00" + e.toString(16)).slice(-2);
            })
            .join("");
        }
        var d = function (e) {
            console.log("蓝牙连接状态变化：", e),
              e.deviceId === r &&
                (e.connected || ((n = !1), (l = !1), u && u()));
          },
          f = function (e) {
            if (e.deviceId === r && e.value) {
              var t = (function (e) {
                e = e.toLowerCase();
                for (var t = [], n = 0, r = 0, a = 0; a < e.length / 2; a++)
                  48 <= (n = e.charCodeAt(2 * a)) && n <= 57
                    ? (n -= 48)
                    : (n = n - 97 + 10),
                    48 <= (r = e.charCodeAt(2 * a + 1)) && r <= 57
                      ? (r -= 48)
                      : (r = r - 97 + 10),
                    (t[a] = 16 * n + r);
                return t;
              })(p(e.value));
              c && c(t);
            }
          },
          _ = function () {
            return new Promise(function (t) {
              e.stopBluetoothDevicesDiscovery({
                success: function (e) {
                  console.log("停止搜索蓝牙设备：", e), t();
                },
                fail: function (e) {
                  console.warn("停止搜索蓝牙设备失败：", e), t();
                },
              });
            });
          },
          h = function () {
            return new Promise(function (t, s) {
              r
                ? e.closeBLEConnection({
                    deviceId: r,
                    success: function () {
                      (n = !1),
                        (r = null),
                        (a = null),
                        (i = null),
                        (o = null),
                        (l = !1),
                        console.log("蓝牙连接已断开"),
                        t();
                    },
                    fail: function (e) {
                      console.log("断开蓝牙连接失败:", e), s(e);
                    },
                  })
                : (console.log("没有已连接的蓝牙设备"), t());
            });
          },
          m = function () {
            return new Promise(function (t, n) {
              var i = 0;
              !(function o() {
                i >= 5
                  ? n(new Error("获取服务超时"))
                  : e.getBLEDeviceServices({
                      deviceId: r,
                      success: function (e) {
                        if (
                          (console.log("获取服务列表：", e),
                          !e.services || 0 === e.services.length)
                        )
                          return i++, void setTimeout(o, 1e3);
                        var r = e.services.find(function (e) {
                          return (
                            "0000FFE0-0000-1000-8000-00805F9B34FB" === e.uuid
                          );
                        });
                        r
                          ? ((a = r.uuid),
                            console.log("找到目标服务ID：", a),
                            v().then(t).catch(n))
                          : (i++, setTimeout(o, 1e3));
                      },
                      fail: function (e) {
                        console.warn("获取服务失败，重试中：", e),
                          i++,
                          setTimeout(o, 1e3);
                      },
                    });
              })();
            });
          },
          v = function () {
            return new Promise(function (t, n) {
              r && a
                ? (e.setBLEMTU({
                    deviceId: r,
                    mtu: 203,
                    fail: function (e) {
                      console.warn("MTU设置失败（不影响基础通信）：", e);
                    },
                  }),
                  e.getBLEDeviceCharacteristics({
                    deviceId: r,
                    serviceId: a,
                    success: function (e) {
                      if (
                        (console.log("获取特征值列表：", e),
                        e.characteristics && 0 !== e.characteristics.length)
                      ) {
                        var r = !1,
                          a = !1;
                        e.characteristics.forEach(function (e) {
                          e.properties.notify &&
                            ((i = e.uuid),
                            (r = !0),
                            console.log("notify ID：", i)),
                            e.properties.write &&
                              ((o = e.uuid),
                              (a = !0),
                              console.log("write ID：", o));
                        }),
                          r && a
                            ? ((l = !0), g().then(t).catch(n))
                            : n(new Error("缺少notify/write特征值，无法通信"));
                      } else n(new Error("未找到任何特征值"));
                    },
                    fail: function (e) {
                      console.log("获取特征值失败：", e), n(e);
                    },
                  }))
                : n(new Error("设备ID/服务ID为空，无法获取特征值"));
            });
          },
          g = function () {
            return new Promise(function (t, n) {
              r && a && i
                ? e.notifyBLECharacteristicValueChange({
                    characteristicId: i,
                    deviceId: r,
                    serviceId: a,
                    state: !0,
                    success: function (n) {
                      console.log("notify启用成功：", n),
                        e.offBLEConnectionStateChange(d),
                        e.onBLEConnectionStateChange(d),
                        e.offBLECharacteristicValueChange(f),
                        e.onBLECharacteristicValueChange(f),
                        t();
                    },
                    fail: function (e) {
                      console.log("notify启用失败：", e), n(e);
                    },
                  })
                : n(new Error("参数不全，无法启用notify"));
            });
          },
          b = function (t) {
            var n = new Uint8Array(
              t.match(/[\da-f]{2}/gi).map(function (e) {
                return parseInt(e, 16);
              })
            );
            return new Promise(function (a, i) {
              e.writeBLECharacteristicValue({
                deviceId: r,
                characteristicId: "0000FFE2-0000-1000-8000-00805F9B34FB",
                serviceId: "0000FFE0-0000-1000-8000-00805F9B34FB",
                value: n.buffer,
                success: function (e) {
                  a();
                },
                fail: function (e) {
                  console.log("备用通道发送数据失败：", t, e), i(e);
                },
              });
            });
          };
        var y = {
          getBluetoothState: function () {
            return new Promise(function (t, r) {
              e.openBluetoothAdapter({
                mode: "central",
                success: function (a) {
                  console.log("蓝牙初始化成功"),
                    e.getBluetoothAdapterState({
                      success: function (e) {
                        console.log("蓝牙状态：", e.available),
                          e.available ? (!0, t()) : (!1, (n = !1), r());
                      },
                      fail: function (e) {
                        console.log("获取蓝牙状态失败：", e),
                          !1,
                          (n = !1),
                          r(-1);
                      },
                    });
                },
                fail: function (e) {
                  !1, (n = !1);
                  var t = 103;
                  10001 === e.errCode
                    ? (t = 10001)
                    : 10002 === e.errCode && (t = 10002),
                    r(t);
                },
              });
            });
          },
          discoveryBluetooth: function () {
            return new Promise(function (t, n) {
              _().finally(function () {
                e.startBluetoothDevicesDiscovery({
                  allowDuplicatesKey: !1,
                  interval: 0,
                  success: function (e) {
                    console.log("开始搜索蓝牙设备：", e),
                      setTimeout(function () {
                        _(), t();
                      }, 5e3);
                  },
                  fail: function (e) {
                    console.log("搜索蓝牙设备失败：", e), n(e);
                  },
                });
              });
            });
          },
          stopDiscoveryBluetooth: _,
          getBluetoothDevices: function () {
            return new Promise(function (t, a) {
              e.getBluetoothDevices({
                success: function (a) {
                  n = !1;
                  var i = a.devices.filter(function (e) {
                      return "" !== e.name && "未知设备" !== e.name;
                    }),
                    o = [];
                  i &&
                    i.forEach(function (e) {
                      ((e.name && "ZX-D30" === e.name.substring(0, 6)) ||
                        (e.name && "GEKOO-BLE" === e.name.substring(0, 9))) &&
                        (o.push(e), console.log("搜索到指定设备：", e.name));
                    }),
                    0 === o.length
                      ? t(!1)
                      : 1 === o.length
                      ? ((s = o[0]), (r = o[0].deviceId), t(!0))
                      : (_(),
                        e.showModal({
                          title: "选择设备",
                          content:
                            "搜索到多个指定设备，您可以手动选择或让系统自动选择信号最强的设备。",
                          confirmText: "自动选择",
                          cancelText: "手动选择",
                          success: function (n) {
                            if (n.confirm) {
                              var a = o.reduce(function (e, t) {
                                return e.RSSI > t.RSSI ? e : t;
                              });
                              (s = a), (r = a.deviceId), t(!0);
                            } else if (n.cancel) {
                              var i = o.map(function (e) {
                                return "GEKOO-BLE" === e.name
                                  ? "GEKOO-BLE (RSSI: ".concat(e.RSSI, ")")
                                  : "GEKOO-Lite (RSSI: ".concat(e.RSSI, ")");
                              });
                              e.showActionSheet({
                                itemList: i,
                                success: function (e) {
                                  var n = e.tapIndex;
                                  (s = o[n]),
                                    console.log("选中设备名称：", o[n].name),
                                    (r = o[n].deviceId),
                                    t(!0);
                                },
                                fail: function () {
                                  t(!1);
                                },
                              });
                            }
                          },
                        }));
                },
                fail: function (e) {
                  console.log("获取蓝牙设备列表失败：", e), (n = !1), a(e);
                },
                complete: function () {
                  console.log("蓝牙设备列表获取完成");
                },
              });
            });
          },
          connectBluetooth: function t() {
            var a =
              arguments.length > 0 && void 0 !== arguments[0]
                ? arguments[0]
                : 3;
            return new Promise(function (i, o) {
              r
                ? e.createBLEConnection({
                    deviceId: r,
                    timeout: 5e3,
                    success: function () {
                      (n = !0),
                        _(),
                        m()
                          .then(function () {
                            i();
                          })
                          .catch(function (e) {
                            h().finally(function () {
                              a > 0
                                ? (console.log(
                                    "服务获取失败，剩余重试次数：".concat(a)
                                  ),
                                  setTimeout(function () {
                                    t(a - 1)
                                      .then(i)
                                      .catch(o);
                                  }, 500))
                                : o(
                                    new Error(
                                      "服务获取失败且重试耗尽：" + e.message
                                    )
                                  );
                            });
                          });
                    },
                    fail: function (e) {
                      (n = !1),
                        console.error(
                          "蓝牙连接失败，错误码：",
                          e.errCode,
                          "信息：",
                          e.errMsg
                        );
                      a > 0 && [10009, 10012, 10013].includes(e.errCode)
                        ? (console.log(
                            "连接失败（错误码"
                              .concat(e.errCode, "），剩余重试次数：")
                              .concat(a)
                          ),
                          h().finally(function () {
                            setTimeout(function () {
                              t(a - 1)
                                .then(i)
                                .catch(o);
                            }, 1500);
                          }))
                        : (_(),
                          o(
                            new Error(
                              "连接失败："
                                .concat(e.errMsg, "（错误码")
                                .concat(e.errCode, "）")
                            )
                          ));
                    },
                  })
                : o(new Error("设备ID为空，无法连接"));
            });
          },
          disconnectBluetooth: h,
          getServiceId: m,
          getCharacteId: v,
          startNotice: g,
          writeData: function (t) {
            if (!0 === l) return (l = !1), b("0102"), Promise.resolve();
            var n = new Uint8Array(
              t.match(/[\da-f]{2}/gi).map(function (e) {
                return parseInt(e, 16);
              })
            );
            return new Promise(function (a, i) {
              e.writeBLECharacteristicValue({
                deviceId: r,
                characteristicId: "0000FFE1-0000-1000-8000-00805F9B34FB",
                serviceId: "0000FFE0-0000-1000-8000-00805F9B34FB",
                value: n.buffer,
                success: function (e) {
                  a();
                },
                fail: function (e) {
                  console.log("主通道发送数据失败：", t, e), i(e);
                },
              });
            });
          },
          writeData2: b,
          ab2hex: p,
          getConnectedDevice: function () {
            return s;
          },
          getConnectState: function () {
            return n;
          },
          setDataCallback: function (e) {
            c = e;
          },
          setDisconnectionCallback: function (e) {
            u = e;
          },
          resetState: function () {
            !1,
              (n = !1),
              !1,
              (r = null),
              (a = null),
              (i = null),
              (o = null),
              (s = null),
              [],
              (l = !1),
              e.offBLEConnectionStateChange(d),
              e.offBLECharacteristicValueChange(f);
          },
        };
        t.default = y;
      }).call(
        this,
        n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2).default
      );
    },
    /*!********************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/utils/realtimedata.js ***!
  \********************************************************************/
    /*! no static exports found */ function (e, t, n) {
      (function (e) {
        var r = n(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
        Object.defineProperty(t, "__esModule", { value: !0 }),
          (t.bitConfig = void 0),
          (t.calc_dashboard = function (e, t) {
            var n,
              r = 0,
              a = (t / 100) * Math.PI,
              i = 0;
            (i = e) < 0 && (i *= -1);
            var o = i / 9e3;
            (n = r = (a * e * 60) / 1e3), r < 10 ? (r = 0) : (r -= 10);
            var s = r / 200;
            o >= 1 && (o = 1);
            s >= 1 && (s = 1);
            return { v_f: o, s_f: s, speed: n };
          }),
          (t.getRecentPasswordRecords = function () {
            return h();
          }),
          (t.parseHardwareModel = function (e) {
            var t = Math.floor(e / 1e3),
              n = Math.floor((e % 1e3) / 100),
              r = Math.floor((e / 10) % 10),
              a = e % 10,
              o = c[t],
              s = i.default[n].subSeries[r].subSeries[a].name,
              u = "".concat(o).concat(s),
              l = i.default[n].subSeries[r].subSeries[a].ibus,
              p = i.default[n].subSeries[r].subSeries[a].iphase;
            return {
              hardwareModel: u,
              busCurrent: l,
              phaseCurrent: p,
              thousandsDigit: t,
            };
          }),
          (t.parseUserCommand = p),
          (t.parseUserCommandNew = d),
          (t.processData = function (e) {
            var t = e[29],
              n = 255 & e[30],
              r = (65280 & e[30]) >> 8,
              a = (255 & e[30]) + "-" + e[26] + e[27];
            6 === Math.floor((t % 1e3) / 100) && (l[0].scale = 218.453);
            n >= 27 && ((l[0].scale = 10), (l[2].scale = 1));
            var i = l.map(function (t) {
                var n = t.id,
                  r = t.index,
                  a = t.scale,
                  i = t.offset,
                  o = t.isSigned,
                  c = t.isFloat,
                  u = e[r];
                o && u > 32767 && (u -= 65536);
                var l = u / a + i;
                return (
                  (l = c ? parseFloat(l.toFixed(2)) : Math.round(l)),
                  2 == n && l < 0 && (l *= -1),
                  s(s({}, t), {}, { value: l })
                );
              }),
              o = JSON.parse(JSON.stringify(u)),
              c = e[23];
            o[0].status = 1 & c ? 0 : 1;
            o[1].status = 4 & c ? 0 : 1;
            o[2].status = 8 & c ? 1 : 0;
            o[3].status = 16 & c ? 0 : 1;
            o[4].status = 32 & c ? 0 : 1;
            o[5].status = 64 & c ? 0 : 1;
            o[6].status = 128 & c ? 0 : 1;
            o[7].status = 256 & c ? 0 : 1;
            var h = "正常",
              m = e[22];
            if (((i[5].value = h), 0 != m))
              for (var v = 0; v < 16; v++)
                if (0 != (m & (1 << v))) {
                  (h = _[v]), (i[5].value = h);
                  break;
                }
            var g,
              b = h,
              y = e[20],
              x = "",
              w = "",
              S = 0;
            if (6 == (y &= 255)) {
              var O = e[19];
              x =
                170 == (255 & O)
                  ? "自学习成功"
                  : 187 == (255 & O)
                  ? f[(g = O >> 8)]
                  : "正在自学习，状态码" + e[19].toString(16);
            } else if (n < 100 && n >= 26) {
              var A = d(e[21], e[20]);
              (x = A.gear_str), (w = A.func_str), (S = A.func_bit);
            } else x = p(e[21]);
            p(e[21]);
            return {
              processedData: i,
              fun_str: w,
              func_bit: S,
              firmware_ver: n,
              hard_ver: t,
              soft_ver: a,
              manufacturer_id: r,
              error_str: b,
              learnself_status_str: x,
              lenarself_error_code: g,
              bitStatus: o,
            };
          }),
          (t.processData1 = function (e) {
            return l.map(function (t) {
              t.id;
              var n = t.index,
                r = t.scale,
                a = t.offset,
                i = t.isSigned,
                o = t.isFloat,
                c = e[n];
              i && c > 32767 && (c -= 65536);
              var u = c / r + a;
              return (
                (u = o ? parseFloat(u.toFixed(2)) : Math.round(u)),
                s(s({}, t), {}, { value: u })
              );
            });
          }),
          (t.realtimedata = void 0),
          (t.savePasswordRecord = function (t) {
            var n = h();
            n.unshift(t), n.length > 10 && n.pop();
            !(function (t) {
              e.setStorageSync("passwordHistory", JSON.stringify(t));
            })(n);
          });
        var a = r(n(/*! @babel/runtime/helpers/defineProperty */ 11)),
          i = r(n(/*! @/utils/hardwareConfig.js */ 66));
        function o(e, t) {
          var n = Object.keys(e);
          if (Object.getOwnPropertySymbols) {
            var r = Object.getOwnPropertySymbols(e);
            t &&
              (r = r.filter(function (t) {
                return Object.getOwnPropertyDescriptor(e, t).enumerable;
              })),
              n.push.apply(n, r);
          }
          return n;
        }
        function s(e) {
          for (var t = 1; t < arguments.length; t++) {
            var n = null != arguments[t] ? arguments[t] : {};
            t % 2
              ? o(Object(n), !0).forEach(function (t) {
                  (0, a.default)(e, t, n[t]);
                })
              : Object.getOwnPropertyDescriptors
              ? Object.defineProperties(e, Object.getOwnPropertyDescriptors(n))
              : o(Object(n)).forEach(function (t) {
                  Object.defineProperty(
                    e,
                    t,
                    Object.getOwnPropertyDescriptor(n, t)
                  );
                });
          }
          return e;
        }
        var c = ["GK", "GK", "GK", "AE", "XM", "UB", "PD", "CP", "ND"];
        var u = [
          { name: "防盗", status: 0 },
          { name: "倒车", status: 0 },
          { name: "高刹", status: 0 },
          { name: "低刹", status: 0 },
          { name: "低速", status: 0 },
          { name: "高速", status: 0 },
          { name: "巡航", status: 0 },
          { name: "边撑", status: 0 },
        ];
        t.bitConfig = u;
        var l = [
          {
            id: 0,
            name: "电压",
            index: 8,
            scale: 273.0666667,
            offset: 0,
            value: 0,
            isSigned: !1,
            isFloat: !0,
          },
          {
            id: 1,
            name: "电流",
            index: 9,
            scale: 1,
            offset: 0,
            value: 0,
            isSigned: !0,
            isFloat: !0,
          },
          {
            id: 2,
            name: "转速",
            index: 6,
            scale: 5.46,
            offset: 0,
            value: 0,
            isSigned: !0,
            isFloat: !0,
          },
          {
            id: 3,
            name: "温度",
            index: 18,
            scale: 100,
            offset: 0,
            value: 1,
            isSigned: !0,
            isFloat: !0,
          },
          {
            id: 4,
            name: "转把",
            index: 2,
            scale: 2450,
            offset: 0,
            value: 0,
            isSigned: !1,
            isFloat: !0,
          },
          {
            id: 5,
            name: "故障",
            index: 22,
            scale: 1,
            offset: 0,
            value: 0,
            isSigned: !1,
            isFloat: !1,
          },
          {
            id: 6,
            name: "5V",
            index: 16,
            scale: 620.6060606,
            offset: 0,
            value: 0,
            isSigned: !1,
            isFloat: !0,
          },
          {
            id: 7,
            name: "12V",
            index: 17,
            scale: 146.0249554,
            offset: 0,
            value: 0,
            isSigned: !1,
            isFloat: !0,
          },
          {
            id: 8,
            name: "电机温度",
            index: 12,
            scale: 100,
            offset: 0,
            value: 0,
            isSigned: !0,
            isFloat: !0,
          },
          {
            id: 9,
            name: "相电流",
            index: 10,
            scale: 1,
            offset: 0,
            value: 0,
            isSigned: !1,
            isFloat: !0,
          },
          {
            id: 10,
            name: "D_23",
            index: 24,
            scale: 1,
            offset: 0,
            value: 0,
            isSigned: !1,
            isFloat: !1,
          },
          {
            id: 11,
            name: "D_25",
            index: 25,
            scale: 1,
            offset: 0,
            value: 0,
            isSigned: !1,
            isFloat: !1,
          },
          {
            id: 12,
            name: "挡位",
            index: 21,
            scale: 1,
            offset: 0,
            value: 0,
            isSigned: !1,
            isFloat: !1,
          },
          {
            id: 13,
            name: "模式",
            index: 20,
            scale: 1,
            offset: 0,
            value: 0,
            isSigned: !1,
            isFloat: !1,
          },
          {
            id: 14,
            name: "自学",
            index: 19,
            scale: 1,
            offset: 0,
            value: 0,
            isSigned: !1,
            isFloat: !1,
          },
          {
            id: 15,
            name: "默认参数",
            index: 13,
            scale: 1,
            offset: 0,
            value: 0,
            isSigned: !1,
            isFloat: !1,
          },
        ];
        function p(e) {
          var t = "停机";
          return (
            0 != (8 & e) && (t = "刹车"),
            1 & e
              ? (t = "防盗锁电机")
              : 2 == ((e >> 8) & 255)
              ? (t = "倒车档")
              : 1 == ((e >> 8) & 255) &&
                (t =
                  1 == ((e >> 4) & 3)
                    ? "高速档"
                    : 2 == ((e >> 4) & 3)
                    ? "低速档"
                    : "中速档"),
            t
          );
        }
        function d(e, t) {
          var n = "",
            r = "",
            a = 0,
            i = (t >> 8) & 15;
          return (
            0 == i
              ? (n = "L挡")
              : 1 == i
              ? (n = "M挡")
              : 2 == i
              ? (n = "H挡")
              : 3 == i
              ? (n = "S挡")
              : 15 == i && (n = "A挡"),
            0 != (4 & e) && (n += " R挡"),
            0 != (8 & e) && (n += " 刹车"),
            0 != (16 & e) && (n += " 边撑"),
            0 != (32 & e) && (n += " P挡"),
            0 != (1 & e) && ((r += "触发防盗 "), (a |= 1)),
            0 != (64 & e) && ((r += "巡航 "), (a |= 2)),
            0 != (128 & e) && ((r += "驻车 "), (a |= 4)),
            0 != (256 & e) && ((r += "EBS "), (a |= 8)),
            0 != (512 & e) && ((r += "助力 "), (a |= 16)),
            0 != (1024 & e) && ((r += "TCS "), (a |= 32)),
            0 != (2048 & e) && ((r += "GB "), (a |= 64)),
            "" == r && ((r = ""), (a = 0)),
            { gear_str: n, func_str: r, func_bit: a }
          );
        }
        t.realtimedata = l;
        var f = [
            "保留",
            "自学习1电流搜索时间超时",
            "自学习1时V相电流偏小",
            "自学习1时W相电流偏小",
            "保留",
            "保留",
            "自学习2电流搜索时间超时",
            "霍尔安装角度搜索失败",
            "霍尔故障",
            "保留",
            "保留",
            "自学习3电流搜索时间超时",
            "自学习3电流2搜索时间超时",
            "自学习3电流偏大",
            "保留",
            "保留",
            "自学习4速度搜索失败",
          ],
          _ = [
            "过流保护",
            "母线过压",
            "母线欠压",
            "过温",
            "电机过温",
            "12V欠压",
            "12V过压",
            "传感器故障1",
            "传感器故障2",
            "霍尔故障",
            "电流过大",
            "堵转保护",
            "5V欠压",
            "5V过压",
            "保留",
            "转把故障",
          ];
        function h() {
          var t = e.getStorageSync("passwordHistory");
          return t ? JSON.parse(t) : [];
        }
      }).call(
        this,
        n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2).default
      );
    },
    /*!****************************************************************!*\
  !*** E:/工程项目/01微信小程序/新版小程序/Senior/GEKOOTECH/utils/firmwave.js ***!
  \****************************************************************/
    /*! no static exports found */ function (e, t, n) {
      (function (e) {
        var r = n(/*! @babel/runtime/helpers/interopRequireDefault */ 4);
        Object.defineProperty(t, "__esModule", { value: !0 }),
          (t.canUpgrade = function (t, n) {
            var r = 0;
            if (!t.includes("-") || !n.includes("-")) return -1;
            var i = n.split("-"),
              o = (0, a.default)(i, 2),
              s = o[0],
              c = o[1],
              u = t.split("-"),
              l = (0, a.default)(u, 2),
              p = l[0],
              d = l[1],
              f = parseInt(s, 10),
              _ = parseInt(p, 10);
            if (isNaN(f) || isNaN(_))
              return (
                e.showToast({
                  title: "主版本号必须为数字",
                  icon: "exception",
                  duration: 2e3,
                }),
                -1
              );
            var h = function (e) {
                if (!e) return "00000000";
                var t = e.replace(/\D/g, "");
                if (8 === t.length) return t;
                if (7 === t.length) {
                  var n = t.slice(0, 4),
                    r = t.slice(4, 5).padStart(2, "0"),
                    a = t.slice(5, 7);
                  return "".concat(n).concat(r).concat(a);
                }
                return "00000000";
              },
              m = h(c),
              v = h(d);
            if (_ <= 16)
              return (
                (r = -1),
                e.showToast({
                  title: "当前版本过低，不支持升级",
                  icon: "exception",
                  duration: 2e3,
                }),
                r
              );
            f > _
              ? (r = 1)
              : f < _
              ? ((r = -1),
                e.showToast({
                  title: "不支持退版本升级",
                  icon: "exception",
                  duration: 2e3,
                }))
              : m > v
              ? (r = 1)
              : m < v
              ? ((r = -1),
                e.showToast({
                  title: "目标版本低于当前版本，不支持升级",
                  icon: "exception",
                  duration: 2e3,
                }))
              : ((r = -1),
                e.showToast({
                  title: "版本已是最新",
                  icon: "none",
                  duration: 2e3,
                }));
            return r;
          }),
          (t.getFirmwareInfo = function (e, t) {
            if (0 == t) {
              if (i.hasOwnProperty(e)) {
                var n = i[e],
                  r = n.updated_ver,
                  a = n.date;
                return "".concat(r, "-").concat(a);
              }
            } else if (2 == t) {
              if (o.hasOwnProperty(e)) {
                var u = o[e],
                  l = u.updated_ver,
                  p = u.date;
                return "".concat(l, "-").concat(p);
              }
            } else if (3 == t) {
              if (s.hasOwnProperty(e)) {
                var d = s[e],
                  f = d.updated_ver,
                  _ = d.date;
                return "".concat(f, "-").concat(_);
              }
            } else if (6 == t && c.hasOwnProperty(e)) {
              var h = c[e],
                m = h.updated_ver,
                v = h.date;
              return "".concat(m, "-").concat(v);
            }
            return null;
          }),
          (t.getUpdateCode = function (e, t) {
            if (0 == t) {
              if (i.hasOwnProperty(e)) {
                var n = i[e],
                  r = n.hex,
                  a = n.index;
                return { hex: r, index: a };
              }
            } else if (2 == t) {
              if (o.hasOwnProperty(e)) {
                var u = o[e],
                  l = u.hex,
                  p = u.index;
                return { hex: l, index: p };
              }
            } else if (3 == t) {
              if (s.hasOwnProperty(e)) {
                var d = s[e],
                  f = d.hex,
                  _ = d.index;
                return { hex: f, index: _ };
              }
            } else if (6 == t && c.hasOwnProperty(e)) {
              var h = c[e],
                m = h.hex,
                v = h.index;
              return { hex: m, index: v };
            }
            return null;
          });
        var a = r(n(/*! @babel/runtime/helpers/slicedToArray */ 5)),
          i = {
            4213: {
              min_ver: 16,
              updated_ver: 24,
              date: 2024112,
              hex: 4,
              index: 0,
            },
            4221: {
              min_ver: 16,
              updated_ver: 24,
              date: 2024112,
              hex: 4,
              index: 1,
            },
            4223: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 4,
              index: 2,
            },
            4233: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 4,
              index: 3,
            },
            4243: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 4,
              index: 4,
            },
            4253: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 4,
              index: 5,
            },
            4272: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 4,
              index: 6,
            },
            4283: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 4,
              index: 7,
            },
            4291: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025630,
              hex: 4,
              index: 8,
            },
            4691: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026129,
              hex: 4,
              index: 9,
            },
            3223: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 3,
              index: 0,
            },
            3233: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 3,
              index: 1,
            },
            3243: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 3,
              index: 2,
            },
            3253: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 3,
              index: 3,
            },
            3284: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 3,
              index: 4,
            },
            3283: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 3,
              index: 5,
            },
            3291: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 3,
              index: 6,
            },
            3251: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 3,
              index: 7,
            },
            3231: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 3,
              index: 8,
            },
            3433: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 3,
              index: 9,
            },
            7223: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 7,
              index: 0,
            },
            7233: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 7,
              index: 1,
            },
            7243: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 7,
              index: 2,
            },
            7253: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 7,
              index: 3,
            },
            7284: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 7,
              index: 4,
            },
            7283: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 7,
              index: 5,
            },
            7291: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 7,
              index: 6,
            },
            7251: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026331,
              hex: 7,
              index: 7,
            },
            7433: {
              min_ver: 26,
              updated_ver: 26,
              date: 2026331,
              hex: 7,
              index: 8,
            },
            7691: {
              min_ver: 26,
              updated_ver: 26,
              date: 2026331,
              hex: 7,
              index: 9,
            },
            7231: {
              min_ver: 26,
              updated_ver: 26,
              date: 2026331,
              hex: 7,
              index: 10,
            },
            8223: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026209,
              hex: 8,
              index: 1,
            },
            8233: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026209,
              hex: 8,
              index: 2,
            },
            8243: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026209,
              hex: 8,
              index: 3,
            },
            8253: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026209,
              hex: 8,
              index: 4,
            },
            8283: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026209,
              hex: 8,
              index: 6,
            },
            8291: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026209,
              hex: 8,
              index: 7,
            },
            8251: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026209,
              hex: 8,
              index: 8,
            },
            8231: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026209,
              hex: 8,
              index: 9,
            },
            8433: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026209,
              hex: 8,
              index: 10,
            },
            6213: {
              min_ver: 16,
              updated_ver: 24,
              date: 2024112,
              hex: 6,
              index: 0,
            },
            6223: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 6,
              index: 1,
            },
            6233: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 6,
              index: 2,
            },
            6243: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 6,
              index: 3,
            },
            6253: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 6,
              index: 4,
            },
            6272: {
              min_ver: 16,
              updated_ver: 25,
              date: 2024422,
              hex: 6,
              index: 5,
            },
            6283: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 6,
              index: 6,
            },
            6291: {
              min_ver: 16,
              updated_ver: 26,
              date: 20251111,
              hex: 6,
              index: 7,
            },
            5213: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026119,
              hex: 5,
              index: 0,
            },
            5223: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026118,
              hex: 5,
              index: 1,
            },
            5233: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026118,
              hex: 5,
              index: 2,
            },
            5243: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026118,
              hex: 5,
              index: 3,
            },
            5253: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026118,
              hex: 5,
              index: 4,
            },
            5283: {
              min_ver: 16,
              updated_ver: 26,
              date: 2026118,
              hex: 5,
              index: 5,
            },
            213: {
              min_ver: 16,
              updated_ver: 24,
              date: 2024112,
              hex: 0,
              index: 0,
            },
            223: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 0,
              index: 2,
            },
            233: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 0,
              index: 3,
            },
            243: {
              min_ver: 16,
              updated_ver: 28,
              date: 2026324,
              hex: 0,
              index: 4,
            },
            253: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 0,
              index: 5,
            },
            272: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 0,
              index: 6,
            },
            283: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 0,
              index: 7,
            },
            291: {
              min_ver: 16,
              updated_ver: 26,
              date: 2025422,
              hex: 0,
              index: 8,
            },
            432: {
              min_ver: 16,
              updated_ver: 27,
              date: 2025606,
              hex: 0,
              index: 11,
            },
          },
          o = {},
          s = {},
          c = {};
      }).call(
        this,
        n(/*! ./node_modules/@dcloudio/uni-mp-weixin/dist/index.js */ 2).default
      );
    },
  ],
]);

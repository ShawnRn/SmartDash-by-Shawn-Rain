var e = require("../static/gk_src1.js"),
  _ = require("../static/gk_src2.js");
module.exports = {
  get_hex_src: function (r) {
    return 0 == r
      ? e.get_hex_src(0)
      : 1 == r
      ? e.get_hex_src(1)
      : 2 == r
      ? e.get_hex_src(2)
      : 3 == r
      ? e.get_hex_src(3)
      : 4 == r
      ? e.get_hex_src(4)
      : 5 == r
      ? _.get_hex_src(0)
      : 6 == r
      ? _.get_hex_src(1)
      : 7 == r
      ? _.get_hex_src(2)
      : 8 == r
      ? _.get_hex_src(3)
      : 9 == r
      ? _.get_hex_src(4)
      : 10 == r
      ? e.get_hex_src(5)
      : 11 == r
      ? e.get_hex_src(6)
      : 12 == r
      ? _.get_hex_src(5)
      : 13 == r
      ? _.get_hex_src(6)
      : null;
  },
};

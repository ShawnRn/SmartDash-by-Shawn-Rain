var e = require("../static/ub_src1.js"),
  r = require("../static/ub_src2.js");
module.exports = {
  get_hex_src: function (s) {
    return 0 == s
      ? e.get_hex_src(0)
      : 1 == s
      ? e.get_hex_src(1)
      : 2 == s
      ? e.get_hex_src(2)
      : 3 == s
      ? e.get_hex_src(3)
      : 4 == s
      ? r.get_hex_src(0)
      : 5 == s
      ? r.get_hex_src(1)
      : null;
  },
};

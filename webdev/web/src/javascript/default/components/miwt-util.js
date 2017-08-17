/**
 MIWT jQuery plugin and global wrapper function.
 Prevents previous and future submit_options from being overridden

 created by jgrossman@proteus.co

 $('.miwt-form').miwt({
  // miwt options
})

 or

 window.$miwt(document.querySelector('.miwt-form'), {
  // miwt options
})

 'this' is bound to form element within miwt option functions

 */
(function ($) {

    // wrapper property getter/setter
    function wrap(value, set) {
        return function (newVal) {
            if (newVal !== undefined) value = set(value, newVal);
            return value;
        };
    }

    // not really function composition
    function compose(f, g) {
        var _this = this;
        return function () {
            f.apply(_this, arguments);
            return g.apply(_this, arguments);
        };
    }

    // composes functions or provides default to oldVal
    function extend(oldVal, newVal) {
        if (oldVal && oldVal instanceof Function) return compose.call(this, oldVal, newVal);else return newVal || oldVal;
    }

    function init(options) {
        // prevents overriding previous submit_options by 'composition'
        var _this = this;
        var submit_options = this.submit_options || {};
        delete this.submit_options; // in case it's already a getter/setter

        // prevents future overriding of these submit_options by 'composition'
        var wrapped = wrap(submit_options, function (oldVal, newVal) {
            for (var i in newVal) {
                oldVal[i] = extend.call(_this, oldVal[i], newVal[i]);
            }
            return oldVal;
        });

        Object.defineProperty(this, 'submit_options', {
            get: wrapped,
            set: wrapped,
            configurable: true // allows deletion of submit_options
        });

        // extend submit_options
        this.submit_options = options;
    }

    // only try if jQuery exists
    $ && $.fn && ($.fn.miwt = function (options) {
        return this.each(function () {
            init.call(this, options);
        });
    });

    // add global, non-jQuery function if no conflict
    !window.$miwt && (window.$miwt = function (el, options) {
        init.call(el, options);
        return el;
    });
})(jQuery);
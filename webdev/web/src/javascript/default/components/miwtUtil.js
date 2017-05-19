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
 */
(function($){

    // wrapper property getter/setter
    function wrap(value, set){
        return function(newVal){
            if(newVal !== undefined) value = set(value,newVal)
            return value;
        }
    }

    // not really function composition
    function compose(f,g){
        return function(){
            f.apply(undefined, arguments);
            return g.apply(undefined, arguments);
        }
    }

    // composes functions or provides default to oldVal
    function extend(oldVal, newVal){
        if(oldVal && oldVal instanceof Function) return compose(oldVal, newVal);
        else return newVal || oldVal;
    }

    function init(options){
        // prevents overriding previous submit_options by 'composition'
        var submit_options = this.submit_options || {};
        delete this.submit_options; // in case it's already a getter/setter
        for(var i in options){
            var option = submit_options[i];
            submit_options[i] = extend(submit_options[i], options[i]);
        }
        this.submit_options = submit_options;

        // prevents future overriding of these submit_options by 'composition'
        var wrapped = wrap(this.submit_options, function(oldVal, newVal){
            var f,g,i,result={};
            for(i in oldVal) result[i] = oldVal[i];
            for(i in newVal){
                g=newVal[i],f=result[i];
                result[i] = extend(f,g);
            }
            return result;
        });

        Object.defineProperty(this, 'submit_options', {
            get: wrapped,
            set: wrapped,
            configurable: true // allows deletion of submit_options
        });
    }

    // only try if jQuery exists
    $ && $.fn && ($.fn.miwt = function(options){
        return this.each(function(){
            init.call(this,options);
        });
    });

    // add global, non-jQuery function if no conflict
    !window.$miwt && (window.$miwt = function(el, options){
        init.call(el,options);
        return el;
    });

}(jQuery));

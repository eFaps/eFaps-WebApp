var profile = (function(){

        copyOnly = function(filename, mid){
            var list = {
                "efaps/efaps.profile":1,
                "efaps/package.json":1
            };
            return (mid in list);
        };

    return {
        resourceTags:{
            copyOnly: function(filename, mid){
                return copyOnly(filename, mid);
            },

            amd: function(filename, mid){
                return !copyOnly(filename, mid);
            },

            miniExclude: function(filename, mid){
                return nodeModulesRe.test(mid);
            }
        }
    };
})();

/*!
 * jQuery.extendext
 *
 * Copyright 2014 Damien "Mistic" Sorel (http://www.strangeplanet.fr)
 * Licensed under MIT (http://opensource.org/licenses/MIT)
 * 
 * Based on jQuery.extend by jQuery Foundation, Inc. and other contributors
 */

(function($){
  "use strict";

  jQuery.extendext = function() {
    var options, name, src, copy, copyIsArray, clone,
      target = arguments[0] || {},
      i = 1,
      length = arguments.length,
      deep = false,
      arrayMode = 'default';

    // Handle a deep copy situation
    if ( typeof target === "boolean" ) {
      deep = target;

      // Skip the boolean and the target
      target = arguments[ i++ ] || {};
    }

    // Handle array mode parameter
    if ( typeof target === "string" ) {
      arrayMode = jQuery([target.toLowerCase(), 'default']).filter(['default','concat','replace','extend'])[0];

      // Skip the string param
      target = arguments[ i++ ] || {};
    }

    // Handle case when target is a string or something (possible in deep copy)
    if ( typeof target !== "object" && !jQuery.isFunction(target) ) {
      target = {};
    }

    // Extend jQuery itself if only one argument is passed
    if ( i === length ) {
      target = this;
      i--;
    }

    for ( ; i < length; i++ ) {
      // Only deal with non-null/undefined values
      if ( (options = arguments[ i ]) != null ) {
        // Special operations for arrays
        if (jQuery.isArray(options) && arrayMode != 'default') {
          clone = target && jQuery.isArray(target) ? target : [];

          switch (arrayMode) {
          case 'concat':
            target = clone.concat( jQuery.extend( deep, [], options ) );
            break;

          case 'replace':
            target = jQuery.extend( deep, [], options );
            break;

          case 'extend':
            options.forEach(function(e, i) {
              if (typeof e === 'object') {
                var type = jQuery.isArray(e) ? [] : {};
                clone[i] = jQuery.extendext( deep, arrayMode, clone[i] || type, e );

              } else if (clone.indexOf(e) === -1) {
                clone.push(e);
              }
            });

            target = clone;
            break;
          }

        } else {
          // Extend the base object
          for ( name in options ) {
            src = target[ name ];
            copy = options[ name ];

            // Prevent never-ending loop
            if ( target === copy ) {
              continue;
            }

            // Recurse if we're merging plain objects or arrays
            if ( deep && copy && ( jQuery.isPlainObject(copy) ||
              (copyIsArray = jQuery.isArray(copy)) ) ) {

              if ( copyIsArray ) {
                copyIsArray = false;
                clone = src && jQuery.isArray(src) ? src : [];

              } else {
                clone = src && jQuery.isPlainObject(src) ? src : {};
              }

              // Never move original objects, clone them
              target[ name ] = jQuery.extendext( deep, arrayMode, clone, copy );

            // Don't bring in undefined values
            } else if ( copy !== undefined ) {
              target[ name ] = copy;
            }
          }
        }
      }
    }

    // Return the modified object
    return target;
  };

}(jQuery));
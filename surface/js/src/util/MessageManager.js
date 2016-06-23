/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
 */
simpl4.util.BaseManager.extend( "simpl4.util.MessageManager", {
	namespaceMap: null,
	transMap: null,
	getMessages: function( namespace ) {
		var failed = function( details ) {
			console.error( "GetMessages(" + namespace + ") failed" + ":" + details.message );
		};

		try {
			var ret = simpl4.util.Rpc.rpcSync( "message:getMessages", {
				namespace: namespace,
				lang: simpl4.util.BaseManager.getLanguage()
			} );
			return ret;
		} catch ( e ) {
			failed( e );
			return [];
		}
	},
	toMap: function( rows ) {
		var transMap = simpl4.util.MessageManager.transMap;
		var count = rows.length;
		for ( var i = 0; i < count; i++ ) {
			var row = rows[ i ];
			transMap[ row.msgid ] = row.msgstr;
		}
	},
	addAliases:function(map){
		Object.keys(map).forEach(function(key) {
			var val = map[key];
			var t = simpl4.util.MessageManager.transMap[ val ];
			simpl4.util.MessageManager.transMap[ key ] = t;
		});
	},
	installMessages: function( namespace ) {
		if ( this.namespaceMap[ namespace ] == null ) {
			var rows = this.getMessages( namespace );
			this.toMap( rows );
			this.namespaceMap[ namespace ] = true;
		}
	},
	installBaseMessages: function() {
		this.namespaceMap = {};
		this.transMap = {};
		this.installMessages( "global" );
		this.installMessages( simpl4.util.BaseManager.getNamespace() );
	},
	tr: function( id ) {
		var t = simpl4.util.MessageManager.transMap[ id ];
		return t == null ? id : t;
	}
}, {} );

/* global {$ ngapp $} */
'use strict';

/**
 * Services that persists and retrieves TODOs from localStorage
 */
{$ ngapp $}.factory('{$ name $}Storage', function () {
	var STORAGE_ID = '{$ ngapp $}';

	return {
		get: function () {
			return JSON.parse(localStorage.getItem(STORAGE_ID) || '[]');
		},

		put: function (objects) {
			localStorage.setItem(STORAGE_ID, JSON.stringify(objects));
		}
	};
});
angular.module('{$ name $}', [
        {%- for dep in angular.deps -%}
            '{$ dep $}'
            {%- if not loop.last %}, {% endif -%}
        {%  endfor %}])
    .config(['$routeProvider', function($routeProvider) {
        $routeProvider

            // route for the home page
            //.when('/', {
            //    templateUrl : 'pages/home.html',
            //    controller  : 'mainController'
            //})

            // route for the about page
          {% for typeid, type in types %}
            .when('/{$ typeid $}', {
                templateUrl : 'template/{$ typeid $}-list-view.html',

                // template : '<div class="panel-body">{$ type.name $}<a class="pull-right" href="#/{$ typeid $}/create"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span></a></div><div class="list-group"><a class="list-group-item" href="#/{$ typeid $}/{{item.id}}/edit" ng-repeat="item in zplItems">{{ item.name }}</a></div>',
                controller  : '{$ typeid $}ListController'
            })

            // route for the contact page
            .when('/{$ typeid $}/create', {
                templateUrl : 'template/{$ typeid $}-edit-view.html',
                controller  : '{$ typeid $}CreateController'
            })

            // route for the contact page
            .when('/{$ typeid $}/:objectid', {
                templateUrl : 'template/{$ typeid $}-edit-view.html',
                controller  : '{$ typeid $}DetailController'
            })

            // route for the contact page
            .when('/{$ typeid $}/:objectid/edit', {
                templateUrl : 'template/{$ typeid $}-edit-view.html',
                controller  : '{$ typeid $}EditController'
            })
          {% endfor %}

            // begin
            .otherwise({
				redirectTo: '/{$ begin $}'
			})
    }])
  {% for typeid, type in types %}
    .factory('{$ typeid $}Storage', function() {
        var STORAGE_ID = '{$ name $}-{$ typeid $}-storage'
        var AUTO_INCREMENT = '{$ name $}-{$ typeid $}-increment'
//        var map = {}
//        var all = []

        return {

            raw: function() {
                return JSON.parse(localStorage.getItem(STORAGE_ID) || '{}');
            },

            save: function(raw) {
                localStorage.setItem(STORAGE_ID, JSON.stringify(raw));
            },

            increment: function() {
                var i = JSON.parse(localStorage.getItem(AUTO_INCREMENT) || '1');
                localStorage.setItem(AUTO_INCREMENT, JSON.stringify(i + 1))
                return i
            },

            find: function () {
                var raw = this.raw()
                var list = []
                for (var key in raw) {
                    if (raw.hasOwnProperty(key) && raw[key]) {
                        list.push(raw[key])
                    }
                }
                return list
            },

            get: function(id) {
                return this.raw()[id]
            },

            put: function (object) {
                var raw = this.raw()
                if (!object.id) {
                    object.id = this.increment()
                }
                raw[object.id] = object
                this.save(raw)
            },

            delete: function (object) {
                var raw = this.raw()
                if (object.id) {
                    delete raw[object.id]
                    this.save(raw)
                }
            }
        }
    })
    .controller('{$ typeid $}ListController', ['$scope', '{$ typeid $}Storage', function($scope, ${$ typeid $}Storage) {
        var storage = ${$ typeid $}Storage
        $scope.type = '{$ typeid $}'
        $scope.zplItems = storage.find()
    }])
    .controller('{$ typeid $}DetailController', ['$scope', '$window', '$routeParams', '{$ typeid $}Storage',
        function($scope, $window, $routeParams, storage) {
            $scope.objectid = $routeParams.objectid
            $scope.obj = storage.get($routeParams.objectid)
            $scope.back = function() {
                $window.history.back();
            }
        }
    ])
    .controller('{$ typeid $}EditController', ['$scope', '$window', '$routeParams', '{$ typeid $}Storage', function($scope, $window, $routeParams, ${$ typeid $}Storage) {
        var storage = ${$ typeid $}Storage
        $scope.objectid = $routeParams.objectid
        $scope.obj = storage.get($routeParams.objectid)
        $scope.back = function() {
            $window.history.back();
        }
        $scope.save = function() {
            storage.put($scope.obj)
            $window.history.back();
        }
        $scope.delete = function() {
            storage.delete($scope.obj)
            $window.history.back();
        }
    }])
    .controller('{$ typeid $}CreateController', ['$scope', '$window', '{$ typeid $}Storage', function($scope, $window, ${$ typeid $}Storage) {
        var storage = ${$ typeid $}Storage
        $scope.hideDelete = true;
        $scope.obj = {}
        $scope.back = function() {
            $window.history.back();
        }
        $scope.save = function() {
            storage.put($scope.obj)
            $window.history.back();
        }
    }])
  {% endfor %}

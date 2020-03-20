/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
angular
	.module('cockpitModule')
	.controller('customChartWidgetEditControllerFunction',customChartWidgetEditControllerFunction)

function customChartWidgetEditControllerFunction(
	$scope,
	finishEdit,
	mdPanelRef,
	sbiModule_translate,
	datastore){
	
	var sort1 = datastore.filter({'city':'Santa Fe'}).sort('city').getDataArray(function(record){
        return {
            name :record.city,
            y:record.total_children
        }
    });;
	
	console.log('sort1',sort1)
    
	var sort2 = datastore.filter({'total_children':'!2'}).sort('city').getDataArray(function(record){
        return {
            name :record.city,
            z:record.total_children
        }
    });
    console.log('sort2', sort2)
    
    var column = datastore.sort('city').getColumn('city');
    console.log('column',column)
    
    var seriesAndData = datastore.getSeriesAndData(function(record){
        return {
            name :record.city,
            children:record.total_children
        }
    },'city');
    console.log('seriesAndData',seriesAndData);
    
	$scope.translate = sbiModule_translate;
	$scope.saveConfiguration=function(){
		 mdPanelRef.close();
		// angular.copy($scope.newModel,model);
		 $scope.$destroy();
		 finishEdit.resolve();
 	}
 	$scope.cancelConfiguration=function(){
 		mdPanelRef.close();
 		$scope.$destroy();
 		finishEdit.reject();
 	}



}

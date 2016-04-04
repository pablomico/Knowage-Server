angular.module('scorecardManager').controller('scorecardPerspectiveDefinitionController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices','$mdDialog','$mdToast','scorecardManager_perspectiveUtility','scorecardManager_semaphoreUtility',scorecardPerspectiveDefinitionControllerFunction ]);


angular.module('scorecardManager').service('scorecardManager_perspectiveUtility',function(scorecardManager_semaphoreUtility){
	this.getPerspectiveStatus=function(perspective){ 
	
	if(angular.equals(perspective.criterion.valueCd,"MAJORITY")){
			return loadPerspectiveByMajority(perspective);
		}else{
			//load by priority
			if(perspective.options.criterionPriority.length==0){
				//if no priority target are selected return the global value 
				return loadPerspectiveByMajority(perspective);
			}else if(perspective.options.criterionPriority.length==1){ 
				// if there is only one target selected, and theyr status in different of GRAY return his status, else the global status
				return (perspective.options.criterionPriority[0].status=="GRAY" || perspective.options.criterionPriority[0].status=="GREEN")  ?   loadPerspectiveByMajority(perspective) : perspective.options.criterionPriority[0].status;
			}
			else{
				return loadPerspectiveByMajorityWithPriority(perspective);
			}
		}
		
	};
	
	function loadPerspectiveByMajorityWithPriority(perspective){
		 var masterPriorityStatus=perspective.options.criterionPriority[0].status
		 for(var i=1;i<perspective.options.criterionPriority.length;i++){
			 masterPriorityStatus=scorecardManager_semaphoreUtility.getPriorityStatus(perspective.options.criterionPriority[i].status,masterPriorityStatus);
		 }
		
		if(angular.equals("GREEN",masterPriorityStatus)){
			return loadPerspectiveByMajority(perspective);
		}else{
			return masterPriorityStatus
		}
		 
	};
	
	function loadPerspectiveByMajority(perspective){ 
		var maxPerspetiveCount=perspective.groupedTargets[0].count;
		var maxPerspetive=perspective.groupedTargets[0].status;
		for(var i=1;i<perspective.groupedTargets.length;i++){
			if(!angular.equals("GRAY",perspective.groupedTargets[i].status)){
				if(perspective.groupedTargets[i].count>maxPerspetiveCount || angular.equals("GRAY",maxPerspetive)){
					maxPerspetiveCount=perspective.groupedTargets[i].count;
					maxPerspetive=perspective.groupedTargets[i].status;
				}else if(perspective.groupedTargets[i].count==maxPerspetiveCount){
					maxPerspetiveCount=perspective.groupedTargets[i].count;
					maxPerspetive=scorecardManager_semaphoreUtility.getPriorityStatus(perspective.groupedTargets[i].status,maxPerspetive);
				}
			}
		} 
		return maxPerspetive ;
	}
	
});




function scorecardPerspectiveDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$mdDialog,$mdToast,scorecardManager_perspectiveUtility,scorecardManager_semaphoreUtility){
	
	$scope.addGroupedTargetsItem=function(type){
		for(var i=0;i<$scope.currentPerspective.groupedTargets.length;i++){
			if(angular.equals($scope.currentPerspective.groupedTargets[i].status,type)){
				$scope.currentPerspective.groupedTargets[i].count++;
				return;
			}
		}
		 $scope.currentPerspective.groupedTargets.push({status:type,count:1});
	}
	
	$scope.addTotalGroupedKpisItem=function(target){
		
		for(var i=0;i<target.groupedKpis.length;i++){
			var tmpGroupedKpis=target.groupedKpis[i];
			var find=false;
			for(var j=0;j<$scope.currentPerspective.groupedKpis.length;j++){
				if(angular.equals( $scope.currentPerspective.groupedKpis[j].status,tmpGroupedKpis.status)){
					$scope.currentPerspective.groupedKpis[j].count+=tmpGroupedKpis.count;
					find=true;
					break;
				}
			}
			if(!find){
				 $scope.currentPerspective.groupedKpis.push({status:tmpGroupedKpis.status,count:tmpGroupedKpis.count});
			}
		}
		
	}
	
	$scope.loadGroupedTarget=function(){
		if($scope.currentPerspective.groupedTargets==undefined){
			 $scope.currentPerspective.groupedTargets=[];
		} 
		for(var i=0;i<$scope.currentPerspective.targets.length;i++){
			$scope.addGroupedTargetsItem($scope.currentPerspective.targets[i].status);
			$scope.addTotalGroupedKpisItem($scope.currentPerspective.targets[i]);
		}
		$scope.currentPerspective.status=scorecardManager_perspectiveUtility.getPerspectiveStatus($scope.currentPerspective);
		
	};
	
	
	
	$scope.$on('savePerspective', function(event, args) {
		 if($scope.currentPerspective.name.trim()==""){
				$scope.showToast('Name is required');
			 return;
		}
		if($scope.currentPerspective.targets==undefined || $scope.currentPerspective.targets.length==0){
			$scope.showToast('Add at least one target');
			 return;
		}

		if ($scope.editProperty.perspective.index != undefined){
			$scope.currentPerspective.groupedTargets=[];
			$scope.loadGroupedTarget();
			angular.copy($scope.emptyPerspective,$scope.currentPerspective[$scope.editProperty.perspective.index]);
		}
		else{
			$scope.loadGroupedTarget();
			$scope.currentScorecard.perspectives.push(angular.extend({},$scope.currentPerspective));
		}
		angular.copy($scope.emptyPerspective,$scope.currentPerspective);

		$scope.stepControl.prevBread();
 	});
	
	$scope.$on('cancelPerspective', function(event, args) {
		if(!angular.equals($scope.emptyPerspective,$scope.currentPerspective)){
	 		var confirm = $mdDialog.confirm()
	        .title(sbiModule_translate.load("sbi.layer.modify.progress"))
	        .content(sbiModule_translate.load("sbi.layer.modify.progress.message.modify"))
	        .ariaLabel('cancel perspective') 
			.ok(sbiModule_translate.load("sbi.general.yes"))
			.cancel(sbiModule_translate.load("sbi.general.No"));
			  $mdDialog.show(confirm).then(function() {
					$scope.stepControl.prevBread();
			  }, function() {
			   return;
			  });
 		}else{
 			$scope.stepControl.prevBread();
 		} 
 	});
	
	$scope.addTarget=function(editTarget, index){ 
		$scope.stepControl.insertBread({name: sbiModule_translate.load('sbi.kpi.scorecard.goal.definition.name')});
		if(editTarget == undefined)
		{
			angular.copy($scope.emptyTarget,$scope.currentTarget);
			$scope.editProperty.target.index = undefined;
		}
		else
			{
				$scope.editProperty.target.index = index;
				angular.copy(editTarget,$scope.currentTarget);
			}
		
	};
}


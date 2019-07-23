"use strict";

angular.module('demoAppModule').controller('IssueBondModalCtrl', function($http, $uibModalInstance, $uibModal, apiBaseURL, peers) {
    const issueBondModal = this;

    issueBondModal.peers = peers;
    issueBondModal.form = {};
    issueBondModal.formError = false;

    /** Validate and create an Bond. */
    issueBondModal.create = () => {
        if (invalidFormInput()) {
            issueBondModal.formError = true;
        } else {
            issueBondModal.formError = false;

            const bondName = issueBondModal.form.bondName;
            const amount = issueBondModal.form.amount;
            const unit = issueBondModal.form.unit;
            const duration = issueBondModal.form.duration;
            const interestRate = issueBondModal.form.interestRate;

            $uibModalInstance.close();

            // We define the Bond creation endpoint.
            const issueBondEndpoint =
                apiBaseURL +
                `issue-bond?bondName=${bondName}&amount=${amount}&pricePerUnit=${unit}&duration=${duration}&i nterestRate=${interestRate}`;

            // We hit the endpoint to create the Bond and handle success/failure responses.
            $http.post(issueBondEndpoint).then(
                (result) => issueBondModal.displayMessage(result),
                (result) => issueBondModal.displayMessage(result)
            );
        }
    };

    /** Displays the success/failure response from attempting to create an Bond. */
    issueBondModal.displayMessage = (message) => {
        const issueBondMsgModal = $uibModal.open({
            templateUrl: 'issueBondMsgModal.html',
            controller: 'issueBondMsgModalCtrl',
            controllerAs: 'issueBondMsgModal',
            resolve: {
                message: () => message
            }
        });

        // No behaviour on close / dismiss.
        issueBondMsgModal.result.then(() => {}, () => {});
    };

    /** Closes the Bond creation modal. */
    issueBondModal.cancel = () => $uibModalInstance.dismiss();

    // Validates the Bond.
    function invalidFormInput() {
        return (issueBondModal.form.bondName === null) || isNaN(issueBondModal.form.amount === 0) || isNaN(issueBondModal.form.unit) || isNaN(issueBondModal.form.duration) || isNaN(issueBondModal.form.interestRate);
    }
});

// Controller for the success/fail modal.
angular.module('demoAppModule').controller('issueBondMsgModalCtrl', function($uibModalInstance, message) {
    const issueBondMsgModal = this;
    issueBondMsgModal.message = message.data;
});
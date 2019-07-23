"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('TransferBondModalCtrl', function ($http, $uibModalInstance, $uibModal, apiBaseURL, peers, id) {
    const transferBondModal = this;

    transferBondModal.peers = peers;
    transferBondModal.id = id;
    transferBondModal.form = {};
    transferBondModal.formError = false;

    transferBondModal.transfer = () => {
        if (invalidFormInput()) {
            transferBondModal.formError = true;
        } else {
            transferBondModal.formError = false;

            const id = transferBondModal.id;
            const party = transferBondModal.form.counterParty;
            const amount = transferBondModal.form.amount;

            $uibModalInstance.close();

            const transferBondEndpoint =
                apiBaseURL +
                `transfer-bond?id=${id}&party=${party}&amount=${amount}`

            $http.get(transferBondEndpoint).then(
                (result) => transferBondModal.displayMessage(result),
                (result) => transferBondModal.displayMessage(result)
            );
        }
    };

    transferBondModal.displayMessage = (message) => {
        const transferBondMsgModal = $uibModal.open({
            templateUrl: 'transferBondMsgModal.html',
            controller: 'transferBondMsgModalCtrl',
            controllerAs: 'transferBondMsgModal',
            resolve: { message: () => message }
        });

        transferBondMsgModal.result.then(() => {}, () => {});
    };

    transferBondModal.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return transferBondModal.form.counterParty === undefined;
    }
});

angular.module('demoAppModule').controller('transferBondMsgModalCtrl', function ($uibModalInstance, message) {
    const transferBondMsgModal = this;
    transferBondMsgModal.message = message.data;
});
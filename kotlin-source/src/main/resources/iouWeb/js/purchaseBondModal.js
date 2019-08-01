"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('PurchaseBondModalCtrl', function ($http, $uibModalInstance, $uibModal, apiBaseURL, bond-name, id) {
    const purchaseBondModal = this;

    purchaseBondModal.bondName = bond-name;
    purchaseBondModal.id = id;
    purchaseBondModal.form = {};
    purchaseBondModal.formError = false;

    purchaseBondModal.purchase = () => {
        if (invalidFormInput()) {
            purchaseBondModal.formError = true;
        } else {
            purchaseBondModal.formError = false;

            const id = purchaseBondModal.id;
            const bondName = purchaseBondModal.form.bondName;
            const amount = purchaseBondModal.form.amount;

            $uibModalInstance.close();

            const purchaseBondEndpoint =
                apiBaseURL +
                `purchase-bond?id=${id}&bondName=${bondName}&amount=${amount}`

            $http.get(purchaseBondEndpoint).then(
                (result) => purchaseBondModal.displayMessage(result),
                (result) => purchaseBondModal.displayMessage(result)
            );
        }
    };

    purchaseBondModal.displayMessage = (message) => {
        const purchaseBondMsgModal = $uibModal.open({
            templateUrl: 'purchaseBondMsgModal.html',
            controller: 'purchaseBondMsgModalCtrl',
            controllerAs: 'purchaseBondMsgModal',
            resolve: { message: () => message }
        });

        purchaseBondMsgModal.result.then(() => {}, () => {});
    };

    purchaseBondModal.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return purchaseBondModal.form.bondName === undefined;
    }
});

angular.module('demoAppModule').controller('purchaseBondMsgModalCtrl', function ($uibModalInstance, message) {
    const purchaseBondMsgModal = this;
    purchaseBondMsgModal.message = message.data;
});
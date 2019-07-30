"use strict";

// Define your backend here.
angular.module('demoAppModule', ['ui.bootstrap']).controller('DemoAppCtrl', function($http, $location, $uibModal) {
    const demoApp = this;

    const apiBaseURL = "/api/iou/";

    // Retrieves the identity of this and other nodes.
    let peers = [];
    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);
    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);
    $http.get(apiBaseURL + "bond-name").then((response) => response.data)

    /** Displays the IOU creation modal. */
    demoApp.openIssueBondModal = () => {
        const issueBondModal = $uibModal.open({
            templateUrl: 'issueBondModal.html',
            controller: 'IssueBondModalCtrl',
            controllerAs: 'issueBondModal',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        // Ignores the modal result events.
        issueBondModal.result.then(() => {}, () => {});
    };

    /** Displays the cash issuance modal. */
    demoApp.openIssueCashModal = () => {
        const issueCashModal = $uibModal.open({
            templateUrl: 'issueCashModal.html',
            controller: 'IssueCashModalCtrl',
            controllerAs: 'issueCashModal',
            resolve: {
                apiBaseURL: () => apiBaseURL
            }
        });

        issueCashModal.result.then(() => {}, () => {});
    };

    demoApp.openPurchaseBondModal = () => {
            const purchaseBondModal = $uibModal.open({
                templateUrl: 'purchaseBondModal.html',
                controller: 'purchaseBondModalCtrl',
                controllerAs: 'purchaseBondModal',
                resolve: {
                    apiBaseURL: () => apiBaseURL
                    bond-name
                }
            });

            purchaseBondModal.result.then(() => {}, () => {});
    };

    /** Displays the IOU transfer modal. */
    demoApp.openTransferBondModal = (id) => {
        const transferBondModal = $uibModal.open({
            templateUrl: 'transferBondModal.html',
            controller: 'TransferBondModalCtrl',
            controllerAs: 'transferBondModal',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                peers: () => peers,
                id: () => id
            }
        });

        transferBondModal.result.then(() => {}, () => {});
    };

    /** Displays the IOU settlement modal. */
    demoApp.openSettleModal = (id) => {
        const settleModal = $uibModal.open({
            templateUrl: 'settleModal.html',
            controller: 'SettleModalCtrl',
            controllerAs: 'settleModal',
            resolve: {
                apiBaseURL: () => apiBaseURL,
                id: () => id
            }
        });

        settleModal.result.then(() => {}, () => {});
    };

    /** Refreshes the front-end. */
    demoApp.refresh = () => {
        // Update the list of IOUs.
        $http.get(apiBaseURL + "ious").then((response) => demoApp.ious =
            Object.keys(response.data).map((key) => response.data[key].state.data));

        // Update the cash balances.
        $http.get(apiBaseURL + "cash-balances").then((response) => demoApp.cashBalances =
            response.data);
    }

    demoApp.refresh();
});

// Causes the webapp to ignore unhandled modal dismissals.
angular.module('demoAppModule').config(['$qProvider', function($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);
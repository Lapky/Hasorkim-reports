'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.notifyReporterStatusChanged = functions.database.ref('/reports/{report}').onUpdate(event => {
    const report = event.params.report;

    const oldStatus = event.data.previous.val().status;
    const newStatus = event.data.val().status;
    const address = event.data.val().address;
    const userId = event.data.previous.val().userId;

    if (!oldStatus || !newStatus || oldStatus == newStatus) {
        return;
    }

    console.log('Report ', report, 'has changed. from: ', oldStatus, "to: ", newStatus, 'User: ', userId);

    // Notification details.
    const payload = {
        data: {
            title: `סטטוס הדיווח ברחוב ${address} השתנה`,
            newStatus: `${newStatus}`,
            report: `${report}`
        }
    };


    var options = {
        priority: "high",
        timeToLive: 60 * 60 * 24
    };

    // Send notifications to user.
    return admin.messaging().sendToDevice(userId, payload, options);

});

exports.notifyManagersAndScannersNewReport = functions.database.ref('/reports/{report}').onCreate(event => {
    const topic = "new_report"

    const report = event.params.report;

    const data_lat = event.data.val().latitude;
    const data_long = event.data.val().longitude;
    const data_address = event.data.val().address;


    if (!data_lat || !data_long || !data_address) {
        return;
    }

    console.log('Report ', report, 'has been created');
    console.log('lat: ', data_lat, ', long: ', data_long, ', address: ', data_address);

    // Notification details.
    const payload = {
        data: {
            lat: `${data_lat}`,
            long: `${data_long}`,
            address: `${data_address}`,
            report: `${report}`,
            type: `notifyManagersAndScannersNewReport`
        }
    };


    var options = {
        priority: "high",
        timeToLive: 60 * 60 * 24
    };

    // Send notifications to user.
    admin.messaging().sendToTopic(topic, payload, options)
      .then(function(response) {
        console.log("Successfully sent message:", response);
      })
      .catch(function(error) {
        console.log("Error sending message:", error);
      });

});

exports.notifyScannerEnlisted = functions.database.ref('/reports/{report}').onUpdate(event => {
    const report = event.params.report;

    const oldAssignedScanner = event.data.previous.val().assignedScanner;
    const newAssignedScanner = event.data.val().assignedScanner;
    const address = event.data.val().address;

    if (!newAssignedScanner || (oldAssignedScanner && (oldAssignedScanner == newAssignedScanner))) {
        return;
    }

    console.log('Report ', report, 'scanner has changed. from: ', oldAssignedScanner, "to: ", newAssignedScanner);

    // Notification details.
    const payload = {
        data: {
            title: `נבחרת לדיווח ברחוב ${address}`,
            report: `${report}`,
            type: `notifyScannerEnlisted`
        }
    };


    var options = {
        priority: "high",
        timeToLive: 60 * 60 * 24
    };

    // Send notifications to user.
    return admin.messaging().sendToDevice(newAssignedScanner, payload, options);

});
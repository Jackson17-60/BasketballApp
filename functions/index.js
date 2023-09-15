const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.notifyParticipantsOnGameDeletion = functions.database
  .ref("/games/{gameId}")
  .onDelete(async (snapshot, context) => {
    // Get the list of participant UIDs
    const participants = snapshot.val().participants;

    // If there are no participants, exit early
    if (!participants) {
      console.log("No participants to notify");
      return null;
    }

    // Get the game details (to include in the notification message)
    const gameDetails = snapshot.val().details;

    // Gather the FCM tokens of all the participants
    const registrationTokens = [];
    for (const uid in participants) {
      try {
        const tokenSnapshot = await admin
          .database()
          .ref(`/users/${uid}/fcmToken`)
          .once("value");
        const token = tokenSnapshot.val();
        if (token) {
          registrationTokens.push(token);
        }
      } catch (error) {
        console.error("Error fetching FCM token for UID:, uid, Error:, error");
      }
    }

    // If there are no valid FCM tokens, exit early
    if (registrationTokens.length === 0) {
      console.log("No valid FCM tokens found");
      return null;
    }

    // Define the notification payload
    const payload = {
      data: {
        title: "Game Cancelled",
        body: `The game scheduled on ${gameDetails.date} at ${gameDetails.time} ` +
             `has been cancelled.`,
      },
      tokens: registrationTokens,
    };

    // Send the notification to all participants
    try {
      const response = await admin.messaging().sendMulticast(payload);
      console.log("Notifications sent successfully:", response);
      return null;
    } catch (error) {
      console.error("Error sending notifications:", error);
      return null;
    }
  });

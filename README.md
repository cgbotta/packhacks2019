# Gift Card Reminder

An Android app that reminds you when you have a gift card to a store you've just walked into.

## How to use

1. Download the APK to your device and run it to install the  app
2. Open the app
3. Click the (+) icon in the top right to add a new card
4. Enter the name of a company for which you have a gift card (example: "target", "starbucks", "walmart")
5. Enter the balance remaining on your card
6. Click "Add"
   ```
   In the background, the app queries the Google Places API to find the 20 nearest 
   locations matching the company you entered (within a 30 mile radius).
   The coordinates of these locations are saved locally in a SQLite database
   ```
7. Download a GPS spoofing application (in lieu of actutally walking into a store) and change your location to be within 200 meters of a store
   for which you have a gift card
8. You should receive a notification reminding you to use your gift card!
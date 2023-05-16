Setup Instructions:
1. Ensure Java 17 is installed (I'm using `java version "17.0.5" 2022-10-18 LTS`)
2. Run `./mvnw clean install -DskipTests`
3. Run the jar `IMAGE_PATH=/tmp/ java -jar <path_to_built_jar>`

Postman Collection Link:
I have described all the fields we are using in the api in this collection. Please let me know if this link doesn't work.
https://api.postman.com/collections/4265735-e599dd9d-1554-4952-9e12-e5e3f3baa09f?access_key=PMAT-01H0JPBYMHN0RBAYMNG3CF0YM0

Testing the App:
1. Make sure app is running
2. Gain access to the postman collection linked above
3. Upload the group of images using `upload segments` request.
4. Get the segment for a set of coordinates in a group using `get segment for group` request.
5. You can get coordinates in the main image in this site `https://pixspy.com/`. Just upload the image and hover. You will see the coordinates.

How is it implemented? 
First of all we can correctly process only PNGs as I didn't have any other file type to test.
When an original image along with its segments is uploaded using upload api, it saves the original image in the path `<IMAGE_PATH>/groupName/0.png`.
Then it saves the segments as `1.png`, `2.png`, ...etc at `<IMAGE_PATH>/groupName`.
After saving the files, it builds a segment map which is basically a 2d array of the same dimensions as the original image.
Then for each segment if the pixel value at a particular coordinate is non-zero then in the segment map we mark that coordinate with this segment's file name.
For example, if we are processing segment 2 its file name would be `2.png`. Then for all the coordinates in `2.png` where the pixel value is non-zero we mark the same coordinates in the segment map with `2`.
After this we compress the segment map and store it in a file called `map` at `<IMAGE_PATH>/groupName`.
I have used Run Length Encoding to compress the file. By using this I have cut down the map size from 17MB to 166KB on disk for the sample group of images I was given.
When `get segment for group` request is called, we read the map file, decompress it and return the segment path for the requested coordinates.

Scenarios where the app might break:
* Files have format other than PNG. 
* If images are given with different dimensions.
* If segments have empty pixel value other than 0.
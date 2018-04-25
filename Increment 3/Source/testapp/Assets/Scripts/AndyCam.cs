using System.IO;
using UnityEngine;

public class AndyCam : MonoBehaviour
{

    private const string DetectObjectImageName = "DetectObjectImage.jpg";
    private byte[] fileData;
    private bool scanningDetectObject;

    private void OnApplicationPause(bool pause)
    {
        if (!pause && scanningDetectObject)
        {
            PhotoRequestEnded();
        }
    }

    // You can call this from the UI
    public void RequestPhoto()
    {
    #if UNITY_ANDROID
        AndroidJavaClass mediaStoreClass = new AndroidJavaClass("android.provider.MediaStore");
        AndroidJavaObject intentObject = new AndroidJavaObject("android.content.Intent");
        intentObject.Call<AndroidJavaObject>("setAction", mediaStoreClass.GetStatic<string>("ACTION_IMAGE_CAPTURE"));

        //define the path and filename to save photo taken by Camera activity
        string filePath = Application.persistentDataPath + Path.DirectorySeparatorChar + DetectObjectImageName;
        AndroidJavaClass uriClass = new AndroidJavaClass("android.net.Uri");
        AndroidJavaObject fileObject = new AndroidJavaObject("java.io.File", filePath);
        AndroidJavaObject uriObject = uriClass.CallStatic<AndroidJavaObject>("fromFile", fileObject);

        intentObject.Call<AndroidJavaObject>("putExtra", mediaStoreClass.GetStatic<string>("EXTRA_OUTPUT"), uriObject);

        // Start the activity
        AndroidJavaClass unity = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject currentActivity = unity.GetStatic<AndroidJavaObject>("currentActivity");
        currentActivity.Call("startActivity", intentObject);

        scanningDetectObject = true;
    #endif
    }

    private void PhotoRequestEnded()
    {
        string DetectObjectImagePath = Application.persistentDataPath + Path.DirectorySeparatorChar + DetectObjectImageName;
        if (File.Exists(DetectObjectImagePath))
        {
            // Load the image into a Texture2D
            fileData = File.ReadAllBytes(DetectObjectImagePath);
            Texture2D texture2D = new Texture2D(2, 2);
            texture2D.LoadImage(fileData);

            var result = "someresult";// DetectObjectUtilities.DecodeDetectObject(texture2D);
            string scannedText = result;//== null ? "" : result.Text;
            OnDetectObjectScanned(scannedText);

            File.Delete(DetectObjectImagePath);
        }
        scanningDetectObject = false;
    }

    private void OnDetectObjectScanned(string scannedText)
    {
        // Here you do what you want with the scanned text
        // scannedText will be empty when the scan fails
    }
}
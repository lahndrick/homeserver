import React, { useState } from "react";
import JSZip from "jszip";
import { saveAs } from "file-saver";
import './App.css';

function App() {
  const [file, setFile] = useState(null);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [imageUrls, setImageUrls] = useState([]);
  const [progress, setProgress] = useState(0);
  const [zipData, setZipData] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [fileError, setFileError] = useState("");

  const maxFileSize = 10 * 1024 * 1024; // 10 MB

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];

    if (selectedFile) {
      if (selectedFile.size > maxFileSize) {
        setFileError("The max upload size is 10MB");
        setFile(null); // reset if it's too large
      } else {
        setFile(selectedFile);
        setFileError("");
      }
    } else {
      setFile(null);
      setFileError("");
    }
  };

  // Handle file upload and process
  const handleSubmit = (e) => {
    e.preventDefault();
    setIsUploading(true);
    setProgress(0);

    const formData = new FormData();
    formData.append("video", file);

    setImageUrls([]);

    const xhr = new XMLHttpRequest();
    xhr.open("POST", "https://rasp.lahndrick.org/video/process", true);

    // Track upload progress
    xhr.upload.onprogress = (progressEvent) => {
      if (progressEvent.lengthComputable) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        setProgress(percent);
      }
    };

    xhr.onload = () => {
      if (xhr.status === 200) {
        setIsModalVisible(true);
        setZipData(xhr.response);
        setIsUploading(false);
      } else {
        console.error("Upload failed", xhr.statusText);
        setIsUploading(false);
      }
    };

    xhr.onerror = () => {
      console.error("Error uploading file.");
      setIsUploading(false);
    };

    xhr.responseType = "arraybuffer"; // Expecting a binary response (the zip file)
    xhr.send(formData);
  };

  // Handle display images in browser
  const handleDisplayImages = () => {
    if (zipData) {
      const zip = new JSZip();
      zip.loadAsync(zipData).then((zipContent) => {
        const imageUrlsArray = [];
        zipContent.forEach((relativePath, file) => {
          if (file.name.endsWith(".jpg") || file.name.endsWith(".png")) {
            zipContent.file(relativePath).async("base64").then((data) => {
              imageUrlsArray.push(`data:image/png;base64,${data}`);
              setImageUrls([...imageUrlsArray]);
            });
          }
        });
      });
    }
    setIsModalVisible(false);
  };

  // Handle zip download
  const handleDownload = () => {
    if (zipData) {
      const blob = new Blob([zipData], { type: "application/zip" });
      saveAs(blob, "processed_images.zip");
      setIsModalVisible(false);
    } else {
      console.error("No zip data available for download.");
    }
  };

  // Check if upload button should be disabled
  const isUploadDisabled = !file || isModalVisible || isUploading;

  return (
    <div className="App">
      <h1>Upload and Process Your MP4 File</h1>

      {/* Upload Section */}
      <form onSubmit={handleSubmit}>
        <input type="file" accept=".mp4" onChange={handleFileChange} />
        <button type="submit" disabled={isUploadDisabled}>
          Upload
        </button>
      </form>

      {/* Show error message if file size exceeds the limit */}
      {fileError && <p style={{ color: 'red' }}>{fileError}</p>}

      {/* Progress bar */}
      {isUploading && (
        <div className="progress-bar-container">
          <div
            className="progress-bar"
            style={{ width: `${progress}%` }}
          ></div>
        </div>
      )}

      {/* Modal for options */}
      {isModalVisible && (
        <div className="modal">
          <div className="modal-content">
            <h3>Choose what to do with the processed images:</h3>
            <div className="modal-buttons">
              <button onClick={handleDisplayImages}>
                Display Images in Browser
              </button>
              <button onClick={handleDownload}>Download ZIP</button>
            </div>
          </div>
        </div>
      )}

      {/* Display the images in the browser */}
      <div className="gallery">
        {imageUrls.length > 0 && (
          <div className="image-container">
            {imageUrls.map((url, index) => (
              <div key={index} className="image-item">
                <img src={url} alt={`Processed Image ${index + 1}`} />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default App;

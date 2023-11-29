 private fun openCamera(type: String) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, 101)
        imageType = if (type == "before") 1 else 2
    }

    private fun openGallery(type: String) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, 102)
        imageType = if (type == "before") 3 else 4
    }

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            
            when (requestCode) {
                    101 -> {
                        //For Camera
                        val photo = data!!.extras?.get("data") as Bitmap
                        var imageUri = getImageUri(this, photo)
                        var bitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            imageUri
                        )
                        val imagePath = bitmap?.let { saveBitmapToFile(it) }
                        if(imageType ==1){
                            repository.insertBeforeImage(projectId, imagePath.toString())
                            updateBeforeImagesAdapter()
                        }
                        else{
                            repository.insertAfterImage(projectId, imagePath.toString())
                            updateAfterImagesAdapter()
                        }
                    }
                    102 -> {
                      //For Gallery
                        val selectedImageUri = data?.data
                        val imagePath = selectedImageUri?.let { saveImageToFile(it) }
                        if(imageType==3){
                            repository.insertBeforeImage(projectId, imagePath.toString())
                            updateBeforeImagesAdapter()
                        }else{
                            repository.insertAfterImage(projectId, imagePath.toString())
                            updateAfterImagesAdapter()
                        }
                    }
                }
        } 
    }

fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "JPEG_${timeStamp}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val contentResolver = inContext.contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { imageUri ->
            try {
                contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                    inImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                return imageUri
            } catch (e: IOException) {
                Log.e(TAG, "Error saving image to MediaStore: ${e.message}")
            }
        }

        return null
    }

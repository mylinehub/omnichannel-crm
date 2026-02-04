package com.mylinehub.crm.whatsapp.service;

import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.whatsapp.api.cloud.wrapper.media.OkHttpUploadDocClient;
import com.mylinehub.crm.whatsapp.dto.service.MediaUploadDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.enums.MEDIA_SELECTION_CRITERIA;
import com.mylinehub.crm.whatsapp.enums.MESSAGE_TYPE;
import com.mylinehub.crm.whatsapp.enums.MESSAGING_PRODUCT;
import com.mylinehub.crm.whatsapp.meta.variables.MESSAGE_SIZE_LIMIT_BYTES;
import com.mylinehub.crm.whatsapp.meta.variables.SUPPORTED_FORMATS;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CallMediaAPIService {

	private final ApplicationContext applicationContext;
	private final OkHttpUploadDocClient okHttpUploadDocClient;

	public MediaUploadDto triggerMediaUploadAPI(String mimeType, Long fileSize, String organizationString, String category, String fileName, WhatsAppPhoneNumber whatsAppPhoneNumber) throws Exception {
		MediaUploadDto toReturn;

		try {
			System.out.println("triggerMediaUploadAPI called");
			System.out.println("Mime Type: " + mimeType + ", File Size: " + fileSize);

			toReturn = findMediaTypeAndVerifyFileSize(mimeType, fileSize);
			System.out.println("Determined media type: " + toReturn.getType() + ", Allowed Upload: " + toReturn.isAllowedUpload());

			toReturn.setExternalPartyUploadSuccessful(true);

			String DIRECTORY = applicationContext.getEnvironment().getProperty("spring.websocket.fileUpload");
			DIRECTORY = DIRECTORY + "/" + organizationString + "/" + category + "/" + fileName;

			JSONObject jsonObject = null;

			if (toReturn.getType() == null || toReturn.getType().isEmpty() || !toReturn.isAllowedUpload()) {
				System.out.println("Unknown or disallowed media type for WhatsApp upload");
				toReturn.setType(mimeType);
				toReturn.setExternalPartyUploadSuccessful(false);
				toReturn.setError("Media type not allowed for upload using WhatsApp Cloud API");
			} else if (toReturn.getType().equals(MESSAGE_TYPE.sticker.name())) {
				System.out.println("Uploading sticker using JSON API...");
				jsonObject = okHttpUploadDocClient.uploadUsingJson(
						MESSAGING_PRODUCT.whatsapp.name(), DIRECTORY, mimeType,
						whatsAppPhoneNumber.getWhatsAppProject().getApiVersion(),
						whatsAppPhoneNumber.getPhoneNumberID(),
						whatsAppPhoneNumber.getWhatsAppProject().getAccessToken());
			} else {
				System.out.println("Uploading media using multipart octet-stream API...");
				jsonObject = okHttpUploadDocClient.uploadUsingMultipart(
						MESSAGING_PRODUCT.whatsapp.name(), DIRECTORY,
						whatsAppPhoneNumber.getWhatsAppProject().getApiVersion(),
						whatsAppPhoneNumber.getPhoneNumberID(),
						whatsAppPhoneNumber.getWhatsAppProject().getAccessToken());
			}

			if (jsonObject != null && jsonObject.has(MEDIA_SELECTION_CRITERIA.id.name())) {
				String mediaId = jsonObject.get(MEDIA_SELECTION_CRITERIA.id.name()).toString();
				System.out.println("Media uploaded successfully. Media ID: " + mediaId);
				toReturn.setMediaId(mediaId);
			} else {
				System.out.println("Upload response did not contain media ID.");
				toReturn.setExternalPartyUploadSuccessful(false);
			}
		} catch (Exception e) {
			System.out.println("Exception in triggerMediaUploadAPI: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return toReturn;
	}

	public byte[] convertBinaryStringToByteArray(String binaryString) {
		System.out.println("convertBinaryStringToByteArray called");

		int stringLength = binaryString.length();
		if (stringLength % 8 != 0) {
			throw new IllegalArgumentException("Binary string length must be a multiple of 8");
		}

		byte[] byteArray = new byte[stringLength / 8];
		for (int i = 0; i < stringLength; i += 8) {
			String byteString = binaryString.substring(i, i + 8);
			int byteValue = Integer.parseInt(byteString, 2);
			byteArray[i / 8] = (byte) byteValue;
		}
		return byteArray;
	}

	public String convertBinaryToByteString(String binaryString) {
		System.out.println("convertBinaryToByteString called");

		if (binaryString == null || binaryString.isEmpty()) {
			return "";
		}

		StringBuilder byteString = new StringBuilder();

		while (binaryString.length() % 8 != 0) {
			binaryString = "0" + binaryString;
		}

		for (int i = 0; i < binaryString.length(); i += 8) {
			String byteSegment = binaryString.substring(i, i + 8);
			int decimalValue = Integer.parseInt(byteSegment, 2);
			byte b = (byte) decimalValue;
			byteString.append((char) b);
		}

		return byteString.toString();
	}

	public MediaUploadDto findMediaTypeAndVerifyFileSize(String mimeType, long fileSize) {
		System.out.println("findMediaTypeAndVerifyFileSize called");
		System.out.println("Checking for mimeType: " + mimeType + ", fileSize: " + fileSize);

		MediaUploadDto toReturn = new MediaUploadDto();
		toReturn.setAllowedUpload(false);
		toReturn.setError("WhatsApp doesn't accept this document format. Try a supported format.");

		try {
			boolean proceed = true;

			if (proceed) {
				for (String audio : SUPPORTED_FORMATS.audio) {
					if (mimeType.contains(audio)) {
						System.out.println("Detected audio format: " + audio);
						toReturn.setType(MESSAGE_TYPE.audio.name());
						proceed = false;

						if (fileSize > MESSAGE_SIZE_LIMIT_BYTES.audio) {
							System.out.println("Audio file too large: " + fileSize);
							toReturn.setAllowedUpload(false);
							toReturn.setError("Audio max allowed size is 16 MB");
						} else {
							toReturn.setAllowedUpload(true);
							toReturn.setError(null);
						}
						break;
					}
				}
			}

			if (proceed) {
				for (String video : SUPPORTED_FORMATS.video) {
					if (mimeType.contains(video)) {
						System.out.println("Detected video format: " + video);
						toReturn.setType(MESSAGE_TYPE.video.name());
						proceed = false;

						if (fileSize > MESSAGE_SIZE_LIMIT_BYTES.video) {
							System.out.println("Video file too large: " + fileSize);
							toReturn.setAllowedUpload(false);
							toReturn.setError("Video max allowed size is 16 MB");
						} else {
							toReturn.setAllowedUpload(true);
							toReturn.setError(null);
						}
						break;
					}
				}
			}

			if (proceed) {
				for (String document : SUPPORTED_FORMATS.document) {
					if (mimeType.endsWith(document)) {
						System.out.println("Detected document format: " + document);
						toReturn.setType(MESSAGE_TYPE.document.name());
						proceed = false;

						if (fileSize > MESSAGE_SIZE_LIMIT_BYTES.document) {
							System.out.println("Document too large: " + fileSize);
							toReturn.setAllowedUpload(false);
							toReturn.setError("Document max allowed size is 100 MB");
						} else {
							toReturn.setAllowedUpload(true);
							toReturn.setError(null);
						}
						break;
					}
				}
			}

			if (proceed) {
				for (String sticker : SUPPORTED_FORMATS.sticker) {
					if (mimeType.contains(sticker)) {
						System.out.println("Detected sticker format: " + sticker);
						toReturn.setType(MESSAGE_TYPE.sticker.name());
						proceed = false;

						if (fileSize > MESSAGE_SIZE_LIMIT_BYTES.sticker) {
							System.out.println("Sticker too large: " + fileSize);
							toReturn.setAllowedUpload(false);
							toReturn.setError("Sticker max allowed size is 100 KB");
						} else {
							toReturn.setAllowedUpload(true);
							toReturn.setError(null);
						}
						break;
					}
				}
			}

			if (proceed) {
				for (String image : SUPPORTED_FORMATS.image) {
					if (mimeType.contains(image)) {
						System.out.println("Detected image format: " + image);
						toReturn.setType(MESSAGE_TYPE.image.name());
						proceed = false;

						if (fileSize > MESSAGE_SIZE_LIMIT_BYTES.image) {
							System.out.println("Image too large: " + fileSize);
							toReturn.setAllowedUpload(false);
							toReturn.setError("Image max allowed size is 5 MB");
						} else {
							toReturn.setAllowedUpload(true);
							toReturn.setError(null);
						}
						break;
					}
				}
			}

		} catch (Exception e) {
			System.out.println("Error in findMediaTypeAndVerifyFileSize: " + e.getMessage());
			e.printStackTrace();
			toReturn.setError(e.getMessage());
			throw e;
		}

		return toReturn;
	}
}

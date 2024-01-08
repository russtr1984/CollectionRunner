package ua.com.runner;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

public class MyUtils {
	static StringBuilder readFile(String pathToFile) {
		StringBuilder content = new StringBuilder();
		try (BufferedReader fileReader = new BufferedReader(new FileReader(pathToFile))) {
			String line;
			while ((line = fileReader.readLine()) != null) {
				content.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	static boolean writeFile(String pathToFile, String content) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathToFile))) {
			writer.write(content);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Помилка запису у файл: " + e.getMessage());
			return false;
		}
	}

	static void log(String fileName, String message) {
		Path filePath = Paths.get(fileName);
		Set<OpenOption> options = Set.of(StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		// Отримуємо поточну дату та час для логу
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		String formattedDateTime = now.format(formatter);
		try (FileChannel fileChannel = FileChannel.open(filePath, options)) {
			// Записати елемент в файл
			String line = formattedDateTime + ": " + Thread.currentThread().getName() + ": " + message + "\n";
			ByteBuffer buffer = ByteBuffer.wrap(line.getBytes());
			// Забезпечте взаємодію тільки з відповідним потоком
			fileChannel.write(buffer);
		} catch (IOException e) {
			e.printStackTrace(); // Обробка помилок запису у файл
		}
	}

	private static String generateRandomMessageId() {
		Random random = new Random();
		return "" + Math.abs(random.nextLong() % 100000000000000000L);
	}

	private static String getCurrentDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
		return dateFormat.format(new Date());
	}
	
	static String fillBody(StringBuilder body, JsonNode vars, JsonNode values)
			throws JsonMappingException, JsonProcessingException {
		body = new StringBuilder(body);
		// fill placeholders {{VAR}}
		int index = body.indexOf("{{");
		while (index != -1) {
			String var = body.substring(index + 2, body.indexOf("}}", index + 2)).trim();
			for (int i = 0; i < vars.size(); i++) {
				if (var.equalsIgnoreCase(vars.get(i).asText())) {
					if (body.charAt(index - 1) == '"' && body.charAt(body.indexOf("}}", index + 2) + 2) == '"') {
						body.replace(index, body.indexOf("}}", index + 2) + 2, values.get(i).asText());
					} else {
						body.replace(index, body.indexOf("}}", index + 2) + 2, "\"" + values.get(i).asText() + "\"");
					}

					break;
				}
			}
			// Знаходимо наступне входження
			index = body.indexOf("{{", index + 2);
		}
		// change messageId and messageDate
		String strBody = body.toString();
		ObjectMapper objMapper = new ObjectMapper();
		JsonNode rootNode = objMapper.readTree(strBody);

		// Зміна значення для поля messageId, якщо воно існує
		JsonNode headerNode = rootNode.path("header");
		JsonNode messageIdNode = headerNode.path("messageId");
		if (!messageIdNode.isMissingNode()) {
			String randomMessageId = generateRandomMessageId();
			((com.fasterxml.jackson.databind.node.ObjectNode) headerNode).put("messageId", randomMessageId);
		}

		// Зміна значення для поля messageDate, якщо воно існує
		JsonNode messageDateNode = headerNode.path("messageDate");
		if (!messageDateNode.isMissingNode()) {
			String currentDateTime = getCurrentDateTime();
			((com.fasterxml.jackson.databind.node.ObjectNode) headerNode).put("messageDate", currentDateTime);
		}

		// Друк оновленого JSON
//		System.out.println(objMapper.writeValueAsString(rootNode));
		return objMapper.writeValueAsString(rootNode);
	}
	
//	static String[][] getVarsFromFile(String fileWithVars) throws Exception {
//		try (BufferedReader fileReader = new BufferedReader(new FileReader(fileWithVars))) {
//			ArrayList<String[]> linesData = new ArrayList<>();
//
//			String line;
//			String[] parts;
//			int counter = 0;
//			int kol_vars = 0;
//			while ((line = fileReader.readLine()) != null) {
//				counter++;
//				// Розділяємо рядок за розділовим знаком (наприклад, комою)
//				parts = line.split(",");
//				if (counter == 1) {
//					kol_vars = parts.length;
//				} else if (kol_vars != parts.length) {
//					System.err.println("Wrong count of variables in the file!");
//					throw new Exception("Wrong count of variables in the file");
//				}
//				// Додаємо масив частин до списку
//				linesData.add(parts);
//			}
//			// Конвертуємо ArrayList у масив строк
//			String[][] linesArray = linesData.toArray(new String[0][0]);
//			return linesArray;
//		}
//	}
}
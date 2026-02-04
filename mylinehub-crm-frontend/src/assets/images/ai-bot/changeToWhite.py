from PIL import Image

# Load the image
image = Image.open("aichatbot-white.png").convert("RGBA")

# Process: Convert all non-transparent pixels to white
pixels = image.load()
width, height = image.size

for y in range(height):
    for x in range(width):
        r, g, b, a = pixels[x, y]
        if a > 0:  # If pixel is not fully transparent
            pixels[x, y] = (255, 255, 255, a)  # Set to white, preserve alpha

# Save the result
image.save("aichatbot_white_only.png", "PNG")

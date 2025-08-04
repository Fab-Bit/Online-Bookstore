FROM python:3.11-slim

# Install dependencies
WORKDIR /app
COPY requirements.txt requirements.txt
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy the test project
COPY . /app

# Create the reports directory at build time so it exists in the container
RUN mkdir -p /app/reports

# Default command executes the pytest suite and writes an HTML report. The
# base URL can be overridden at runtime via the BASE_URL environment variable.
CMD ["pytest", "--html=reports/report.html", "--self-contained-html"]

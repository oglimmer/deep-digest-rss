#!/usr/bin/env bash

set -euo pipefail

# Define script metadata
SCRIPT_NAME=$(basename "$0")
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Default configuration
REGISTRY="${REGISTRY:-registry.oglimmer.com}"
API_URL="${API_URL:-https://api-news.oglimmer.com}"
TAG="${TAG:-latest}"
VERBOSE="${VERBOSE:-false}"
DRY_RUN="${DRY_RUN:-false}"
BUILD_ONLY="${BUILD_ONLY:-false}"
PUSH_ONLY="${PUSH_ONLY:-false}"

# Component configuration (bash 3.2+ compatible)
ALL_COMPONENTS="news-frontend news-backend scraper auth"

# Get image tag for a component
get_image_tag() {
  local component="$1"
  case "$component" in
    news-frontend) echo "${REGISTRY}/news-frontend:${TAG}" ;;
    news-backend)  echo "${REGISTRY}/news-backend:${TAG}" ;;
    scraper)       echo "${REGISTRY}/news-scraper:${TAG}" ;;
    auth)          echo "${REGISTRY}/auth-module:${TAG}" ;;
    *) echo ""; return 1 ;;
  esac
}

# Get build args for a component
get_build_args() {
  local component="$1"
  case "$component" in
    news-frontend) echo "--build-arg API_URL=${API_URL}" ;;
    *) echo "" ;;
  esac
}

# Check if component is valid
is_valid_component() {
  local component="$1"
  local comp
  for comp in ${ALL_COMPONENTS}; do
    [[ "$comp" == "$component" ]] && return 0
  done
  return 1
}

# Color output (only if terminal supports it)
if [[ -t 1 ]] && command -v tput >/dev/null 2>&1; then
  BOLD="$(tput bold)"
  GREEN="$(tput setaf 2)"
  YELLOW="$(tput setaf 3)"
  RED="$(tput setaf 1)"
  RESET="$(tput sgr0)"
else
  BOLD="" GREEN="" YELLOW="" RED="" RESET=""
fi

# Display usage information
usage() {
  cat << EOF
Usage: ${SCRIPT_NAME} [OPTIONS] [COMPONENT...]

Build and push Docker images for the Deep Digest RSS project.

OPTIONS:
  -h, --help          Show this help message
  -v, --verbose       Enable verbose output (set -x)
  -n, --dry-run       Show what would be done without executing
  -b, --build-only    Only build images, skip pushing
  -p, --push-only     Only push images, skip building
  -t, --tag TAG       Set image tag (default: latest)
  -r, --registry URL  Set registry URL (default: registry.oglimmer.com)
  -a, --api-url URL   Set API URL for frontend build (default: https://api-news.oglimmer.com)
  --all               Build/push all components (same as specifying all component names)

COMPONENTS:
  news-frontend       Build/push frontend component
  news-backend        Build/push backend component
  scraper             Build/push scraper component
  auth                Build/push auth module

  You must specify at least one component or use --all.

ENVIRONMENT VARIABLES:
  REGISTRY           Docker registry URL
  API_URL            API URL for frontend
  TAG                Docker image tag
  VERBOSE            Enable verbose mode (true/false)
  DRY_RUN            Enable dry-run mode (true/false)
  BUILD_ONLY         Only build, don't push (true/false)
  PUSH_ONLY          Only push, don't build (true/false)

EXAMPLES:
  ${SCRIPT_NAME} --all                     # Build and push all components
  ${SCRIPT_NAME} --all -b                  # Build all, don't push
  ${SCRIPT_NAME} --all -t v1.2.3           # Build and push with tag v1.2.3
  ${SCRIPT_NAME} news-frontend scraper     # Build and push only frontend and scraper
  ${SCRIPT_NAME} --all -n -v               # Dry-run all with verbose output
  ${SCRIPT_NAME} -a https://api.example.com news-frontend  # Custom API URL

EOF
  exit "${1:-0}"
}

# Validate required programs are installed
validate_dependencies() {
  local missing_deps=()

  if ! command -v docker >/dev/null 2>&1; then
    missing_deps+=("docker")
  fi

  if [[ ${#missing_deps[@]} -gt 0 ]]; then
    echo "${RED}Error: Missing required dependencies: ${missing_deps[*]}${RESET}" >&2
    echo "Please install the missing dependencies and try again." >&2
    exit 1
  fi

  # Check if Docker daemon is running
  if ! docker info >/dev/null 2>&1; then
    echo "${RED}Error: Docker daemon is not running${RESET}" >&2
    echo "Please start Docker and try again." >&2
    exit 1
  fi
}

# Execute command with dry-run support
execute() {
  local cmd="$*"

  if [[ "${DRY_RUN}" == "true" ]]; then
    echo "${YELLOW}[DRY-RUN]${RESET} ${cmd}"
  else
    [[ "${VERBOSE}" == "true" ]] && echo "${GREEN}[EXEC]${RESET} ${cmd}"
    eval "${cmd}"
  fi
}

# Build a Docker image
build_image() {
  local component="$1"
  local image_tag
  local build_args

  image_tag="$(get_image_tag "$component")"
  build_args="$(get_build_args "$component")"

  echo "${BOLD}Building ${component}...${RESET}"

  if [[ ! -d "${SCRIPT_DIR}/${component}" ]]; then
    echo "${RED}Error: Component directory '${component}' not found${RESET}" >&2
    return 1
  fi

  execute docker build ${build_args} --tag "${image_tag}" "${SCRIPT_DIR}/${component}"

  echo "${GREEN}✓ Built ${image_tag}${RESET}"
}

# Push a Docker image
push_image() {
  local component="$1"
  local image_tag

  image_tag="$(get_image_tag "$component")"

  echo "${BOLD}Pushing ${component}...${RESET}"

  execute docker push "${image_tag}"

  echo "${GREEN}✓ Pushed ${image_tag}${RESET}"
}

# Build all specified components
build_all() {
  local components=("$@")
  local failed=()

  for component in "${components[@]}"; do
    if ! build_image "${component}"; then
      failed+=("${component}")
    fi
  done

  if [[ ${#failed[@]} -gt 0 ]]; then
    echo "${RED}Failed to build: ${failed[*]}${RESET}" >&2
    return 1
  fi
}

# Push all specified components
push_all() {
  local components=("$@")
  local failed=()

  for component in "${components[@]}"; do
    if ! push_image "${component}"; then
      failed+=("${component}")
    fi
  done

  if [[ ${#failed[@]} -gt 0 ]]; then
    echo "${RED}Failed to push: ${failed[*]}${RESET}" >&2
    return 1
  fi
}

# Main function
main() {
  local selected_components=()

  # Show help if no arguments provided
  if [[ $# -eq 0 ]]; then
    usage 0
  fi

  # Parse command-line arguments
  local use_all_components=false

  while [[ $# -gt 0 ]]; do
    case "$1" in
      -h|--help)
        usage 0
        ;;
      --all)
        use_all_components=true
        shift
        ;;
      -v|--verbose)
        VERBOSE=true
        set -x
        shift
        ;;
      -n|--dry-run)
        DRY_RUN=true
        shift
        ;;
      -b|--build-only)
        BUILD_ONLY=true
        shift
        ;;
      -p|--push-only)
        PUSH_ONLY=true
        shift
        ;;
      -t|--tag)
        [[ -z "${2:-}" ]] && { echo "${RED}Error: --tag requires an argument${RESET}" >&2; usage 1; }
        TAG="$2"
        shift 2
        ;;
      -r|--registry)
        [[ -z "${2:-}" ]] && { echo "${RED}Error: --registry requires an argument${RESET}" >&2; usage 1; }
        REGISTRY="$2"
        shift 2
        ;;
      -a|--api-url)
        [[ -z "${2:-}" ]] && { echo "${RED}Error: --api-url requires an argument${RESET}" >&2; usage 1; }
        API_URL="$2"
        shift 2
        ;;
      -*)
        echo "${RED}Error: Unknown option: $1${RESET}" >&2
        usage 1
        ;;
      *)
        # Component name
        if is_valid_component "$1"; then
          selected_components+=("$1")
        else
          echo "${RED}Error: Unknown component: $1${RESET}" >&2
          echo "Valid components: ${ALL_COMPONENTS}" >&2
          exit 1
        fi
        shift
        ;;
    esac
  done

  # Validate conflicting options
  if [[ "${BUILD_ONLY}" == "true" ]] && [[ "${PUSH_ONLY}" == "true" ]]; then
    echo "${RED}Error: Cannot use both --build-only and --push-only${RESET}" >&2
    usage 1
  fi

  # Handle --all flag or check if components were specified
  if [[ "$use_all_components" == "true" ]]; then
    local comp
    for comp in ${ALL_COMPONENTS}; do
      selected_components+=("$comp")
    done
  elif [[ ${#selected_components[@]} -eq 0 ]]; then
    echo "${RED}Error: No components specified. Use --all or specify component names.${RESET}" >&2
    echo "Run '${SCRIPT_NAME} --help' for usage information." >&2
    exit 1
  fi

  # Sort components for consistent output
  IFS=$'\n' selected_components=($(sort <<<"${selected_components[*]}"))
  unset IFS

  # Validate dependencies
  validate_dependencies

  # Display configuration
  echo "${BOLD}=== Build Configuration ===${RESET}"
  echo "Registry:     ${REGISTRY}"
  echo "Tag:          ${TAG}"
  echo "API URL:      ${API_URL}"
  echo "Components:   ${selected_components[*]}"
  echo "Build-only:   ${BUILD_ONLY}"
  echo "Push-only:    ${PUSH_ONLY}"
  echo "Dry-run:      ${DRY_RUN}"
  echo "${BOLD}===========================${RESET}"
  echo

  # Execute build and/or push
  if [[ "${PUSH_ONLY}" != "true" ]]; then
    echo "${BOLD}${GREEN}>>> Building images...${RESET}"
    build_all "${selected_components[@]}" || exit 1
    echo
  fi

  if [[ "${BUILD_ONLY}" != "true" ]]; then
    echo "${BOLD}${GREEN}>>> Pushing images...${RESET}"
    push_all "${selected_components[@]}" || exit 1
    echo
  fi

  echo "${BOLD}${GREEN}✓ All operations completed successfully${RESET}"
}

# Run main function
main "$@"

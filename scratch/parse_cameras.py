import re

def parse_line_by_line(file_path):
    with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
        lines = f.readlines()
        
    current_device = None
    device_info = {}
    
    for i, line in enumerate(lines):
        # Detect device header
        m = re.match(r'== Camera HAL device (device@\d+\.\d+/\w+/\d+).*?static information: ==', line)
        if m:
            if current_device:
                print_device(current_device, device_info)
            current_device = m.group(1)
            device_info = {
                'facing': 'Unknown',
                'zoom': 'Unknown',
                'focal': 'Unknown',
                'physical': 'None',
                'logical': False
            }
            continue
            
        # Detect end of static info
        if current_device and (line.startswith('== Camera HAL device') and 'dumpState' in line or line.startswith('== Camera Provider HAL')):
            print_device(current_device, device_info)
            current_device = None
            continue
            
        if current_device:
            # Parse facing
            if 'Facing:' in line:
                device_info['facing'] = line.split('Facing:')[1].strip()
            # Parse zoom
            elif 'android.control.zoomRatioRange' in line:
                # zoom values are on the next line
                next_line = lines[i+1]
                zoom_m = re.search(r'\[\s*([\d\.]+)\s+([\d\.]+)\s*\]', next_line)
                if zoom_m:
                    device_info['zoom'] = f"{zoom_m.group(1)}..{zoom_m.group(2)}"
            # Parse focal lengths
            elif 'android.lens.info.availableFocalLengths' in line:
                next_line = lines[i+1]
                focal_m = re.search(r'\[\s*([\d\.\s]+)\s*\]', next_line)
                if focal_m:
                    device_info['focal'] = focal_m.group(1).strip().replace('\n', ' ')
            # Parse physical IDs
            elif 'android.logicalMultiCamera.physicalIds' in line:
                next_line = lines[i+1]
                phys_m = re.search(r'\[\s*(.*?)\s*\]', next_line)
                if phys_m:
                    device_info['physical'] = phys_m.group(1).strip()
            # Parse logical capabilities
            elif 'LOGICAL_MULTI_CAMERA' in line:
                device_info['logical'] = True

    if current_device:
        print_device(current_device, device_info)

def print_device(device_id, info):
    print(f"Device: {device_id}")
    print(f"  Facing: {info['facing']}")
    print(f"  Zoom Range: {info['zoom']}")
    print(f"  Focal Lengths: {info['focal']}")
    print(f"  Physical IDs: {info['physical']}")
    print(f"  Logical: {info['logical']}")
    print("-" * 50)

if __name__ == "__main__":
    parse_line_by_line("/Users/shawnrain/Library/Mobile Documents/com~apple~CloudDocs/Shawn Rain/Vibe-Coding/SmartDash/scratch/camera_dumpsys.txt")
